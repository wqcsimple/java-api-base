package com.whis.base.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.whis.base.common.Util;
import com.whis.base.exception.BaseException;
import com.whis.base.exception.NotExistsException;
import com.whis.base.model.BaseModel;
import com.whis.base.model.Model;
import com.whis.base.tx.JOOQExceptionTranslator;
import org.jooq.*;
import org.jooq.conf.RenderNameStyle;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

import static org.jooq.impl.DSL.*;

/**
 * Created by dd on 21/07/2017.
 */
public class CoreQuery {

    private static Logger logger = LoggerFactory.getLogger(CoreQuery.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, String[]> modelKeysCache = new HashMap<>();

    private static Configuration configuration = new DefaultConfiguration();

    private static volatile CoreQuery instance;
    public static CoreQuery getInstance() {
        if (instance == null) {
            synchronized (CoreQuery.class) {
                if (instance == null) {
                    instance = new CoreQuery();

                    DataSourceConnectionProvider connectionProvider = new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(Core.getDataSource()));
                    // configuration.set(Core.getBean(SpringTransactionProvider.class));
                    configuration.set(connectionProvider);
                    configuration.set(SQLDialect.MYSQL);

                    DefaultExecuteListenerProvider executeListenerProvider = new DefaultExecuteListenerProvider(new JOOQExceptionTranslator());
                    configuration.set(executeListenerProvider);

                    configuration.settings().setRenderNameStyle(RenderNameStyle.QUOTED);
                }
            }
        }
        return instance;
    }


    public DSLContext getDSLContext()
    {
        return DSL.using(configuration);
    }

    public JdbcTemplate getJdbcTemplate() {
        return Core.getBean(JdbcTemplate.class);
    }

    private String getTableName(Class<?> modelClass) {
        Model model = modelClass.getAnnotation(Model.class);
        if (model == null) {
            throw new BaseException(-1, "" + modelClass.toString() + " must have annotation: Model");
        }
        return model.tableName();
    }

    private Model getModel(Class<?> modelClass) {
        Model model = modelClass.getAnnotation(Model.class);
        if (model == null) {
            throw new BaseException(-1, "" + modelClass.toString() + " must have annotation: Model");
        }
        return model;
    }

    private void ensureModelClass(Class cls) {
        if (!BaseModel.class.isAssignableFrom(cls)) {
            throw new BaseException(-1, cls + " is not BaseModel");
        }
    }

    private Field[] getFields(BaseModel model) {
        String[] keys = model.keys();
        Field[] fields = new Field[keys.length];
        for (int i = 0; i < keys.length; i++) {
            fields[i] = field(keys[i]);
        }
        return fields;
    }

    public String[] keys(BaseModel model) {
        return Lists.newArrayList(model.keys()).stream().filter(item -> !item.equals("id")).toArray(String[]::new);
    }

    public int insert(BaseModel model) {
        String[] keys = keys(model);
        logger.trace("insert {}: keys {}", model.getClass().toString(), Util.jsonEncode(keys));

        DSLContext ctx = getDSLContext();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        List<String> keyList = Lists.newArrayList(keys);
        Field[] columnList = keyList.stream().map(item -> field(name(item))).toArray(Field[]::new);
        Object[] valueList = keyList.stream().map(model::get).toArray();

        Query query = ctx.insertInto(table(name(getTableName(model.getClass()))), columnList).values(valueList);
        logger.trace("insert: {}", query.getSQL());
        // jdbcTemplate.update(query.getSQL(), query.getBindValues().toArray());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int count = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(query.getSQL(), Statement.RETURN_GENERATED_KEYS);
                    for (int i = 0; i < valueList.length; i++) {
                        ps.setObject(i + 1, valueList[i]);
                    }
                    return ps;
                },
                keyHolder);
        model.set("id", keyHolder.getKey().longValue());
        return count;
    }

    public int update(BaseModel model) {
        if (model.ID() == null || model.ID() <= 0) {
            throw new BaseException(-1, "can not update a new model");
        }

        String[] keys = model.keys();

        DSLContext dsl = getDSLContext();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        List<String> keyList = Lists.newArrayList(keys);
        Map<Field, Object> data = new HashMap<>();
        keyList.forEach(key -> { data.put(field(name(key)), model.get(key)); });

        Query query = dsl.update(table(name(getTableName(model.getClass())))).set(data)
                .where(field("id").eq(model.ID()));
        logger.trace("update: {}, {}", query.getSQL(), query.getBindValues().toArray());
        return jdbcTemplate.update(query.getSQL(), query.getBindValues().toArray());
    }


    @Nullable
    public <T> T findById(Class<T> c, Long id) {
        return findByCol(c,"id", id);
    }

    @Nullable
    public <T> T findByCol(Class<T> c, String colName, Object colValue) {
        return findByCols(c, colName, colValue);
    }

    @Nullable
    public <T> T findByCols(Class<T> c, Object... args) {
        ensureModelClass(c);

        List<T> resultList = queryByCols(c, args);
        if (resultList.size() > 0) {
            return resultList.get(0);
        }

        return null;
    }

    @Nullable
    public <T> T findByCols(Class<T> c, Map<String, Object> kvs) {
        List<T> resultList = queryByCols(c, kvs);
        if (resultList.size() > 0) {
            return resultList.get(0);
        }

        return null;
    }

    private <T> List<T> processQueryResultMaps(Class<T> c, List<Map<String, Object>> maps) {
        return Core.M().convertMapListToModelList(maps, c);
    }

    public <T> List<T> queryByCols(Class<T> c, Object... args) {
        return processQueryResultMaps(c, queryRawByCols(c, args));
    }

    public <T> List<T> queryByCols(Class<T> c, Map<String, Object> kvs) {
        return processQueryResultMaps(c, queryRawByCols(c, kvs));
    }

    public <T> List<Map<String, Object>> queryRawByCols(Class<T> c, Object... args) {
        ensureModelClass(c);

        if (args.length == 0 || args.length % 2 != 0) {
            return null;
        }

        List<Condition> conditionList = new ArrayList<>();

        for (int i = 0; i < args.length / 2; i++)
        {
            if (!(args[i * 2] instanceof String))
            {
                return null;
            }
            String colName = (String) args[i * 2];
            Object colValue = args[i * 2 + 1];
            conditionList.add(field(colName).eq(colValue));
        }

        return queryRawByCols(c, conditionList);
    }

    public <T> List<Map<String, Object>> queryRawByCols(Class<T> c, Map<String, Object> kvs) {
        ensureModelClass(c);

        if (kvs == null || kvs.size() == 0) {
            return null;
        }

        Collection<Condition> conditions = new ArrayList<>();

        for (String key : kvs.keySet()) {
            Object value = kvs.get(key);
            conditions.add(field(key).eq(value));
        }

        return queryRawByCols(c, conditions);
    }

    public <T> List<Map<String, Object>> queryRawByCols(Class<T> c, List<Condition> conditions) {
        ensureModelClass(c);

        if (conditions == null || conditions.size() == 0) {
            return null;
        }

        DSLContext dsl = getDSLContext();
        Model model = getModel(c);

        if (model.enableWeight()) {
            conditions.add(field("weight").greaterOrEqual(0));
        }

        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        Query query = dsl.select()
                .from(name(model.tableName()))
                .where(conditions);

        List<Map<String, Object>> resultMapList = jdbcTemplate.queryForList(query.getSQL(), query.getBindValues().toArray());
        if (resultMapList == null || resultMapList.size() == 0) {
            return null;
        }

        return resultMapList;
    }

    public <T> T findOrFail(Class<T> c, Long id) {
        T map = findById(c, id);
        if (map == null) {
            throw new NotExistsException();
        }

        return map;
    }

    public <T> void updateCol(Class<T> c, Long id, String colName, Object colValue) {
        DSLContext dsl = getDSLContext();

        logger.info("update col: {} -> {}, id: {}", colName, colValue, id);

        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        Query query = dsl.update(table(name(getTableName(c))))
                .set(field(colName), colValue)
                .where(field("id").eq(id));
        jdbcTemplate.update(query.getSQL(), query.getBindValues().toArray());
    }

    public <T> void updateCols(Class<T> c, Long id, Object... args) {
        if (args.length == 0 || args.length % 2 != 0) {
            logger.debug("args invalid: {} {}", id, args);
            return ;
        }

        logger.info("update cols: , id: {}", args, id);

        DSLContext dsl = getDSLContext();
        String firstColName = args[0].toString();
        Object firstColValue = args[1];
        UpdateSetMoreStep step = dsl.update(table(name(getTableName(c))))
                .set(field(firstColName), firstColValue);

        for (int i = 1; i < args.length / 2; i++)
        {
            if (!(args[i * 2] instanceof String))
            {
                return;
            }
            String colName = (String) args[i * 2];
            Object colValue = args[i * 2 + 1];
            step = step.set(field(colName), colValue);
        }
        Query query = step.where(field("id").eq(id));

        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        jdbcTemplate.update(query.getSQL(), query.getBindValues().toArray());
    }

    public <T> SelectQuery<Record> createQuery(Class<T> c) {
        DSLContext dsl = getDSLContext();
        return dsl.select().from(name(getTableName(c))).getQuery();
    }

    public <T> SelectQuery<Record> createQuery(Class<T> c, List<Condition> conditions) {
        DSLContext dsl = getDSLContext();
        SelectQuery<Record> query = dsl.select().from(name(getTableName(c))).getQuery();
        query.addConditions(conditions);
        return query;
    }

    public <T> SelectQuery<Record> createQuery(Class<T> c, Field... cols) {
        DSLContext dsl = getDSLContext();
        return dsl.select(cols).from(name(getTableName(c))).getQuery();
    }

    public <T> SelectQuery<Record> createQueryByPage(Class<T> c, int limit, int offset) {
        SelectQuery<Record> query = createQuery(c);
        query.addLimit(limit);
        query.addOffset(offset);
        return query;
    }

    public List<Map<String, Object>> executeQuery(SelectQuery<Record> query) {
        if (query == null) {
            throw new BaseException(-1, "query must no be null");
        }

        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        return jdbcTemplate.queryForList(query.getSQL(), query.getBindValues().toArray());
    }

    public List executeQueryAndCollectSingleColumn(SelectQuery<Record> query, String col) {
        if (query == null) {
            throw new BaseException(-1, "query must no be null");
        }

        List<Map<String, Object>> resultList = executeQuery(query);
        List list = new ArrayList();
        if (resultList != null && resultList.size() > 0) {
            resultList.forEach((Map<String, Object> record) -> {
                if (!record.containsKey(col)) {
                    throw new BaseException(-1, "record doesn't contains col");
                }
                list.add(record.get(col));
            });
        }
        return list;
    }

    public int count(SelectQuery<Record> query) {
        return DSL.using(Core.getDataSource(), SQLDialect.MYSQL).fetchCount(query);
    }

    @Nullable
    public Object queryScalar(SelectQuery<Record> query) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(query.getSQL(), query.getBindValues().toArray());
        if (rowSet == null || !rowSet.next()) {
            return null;
        }
        return rowSet.getObject(1);
    }

    @Nullable
    public Object queryScalar(String sql, Object... args) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, args);
        if (rowSet == null || !rowSet.next()) {
            return null;
        }
        return rowSet.getObject(1);
    }


    @Nullable
    public <T> T queryOne(Class<T> c, SelectQuery<Record> query) {
        ensureModelClass(c);
        Map<String, Object> map = queryOne(query);
        return Core.M().convertMapToModel(map, c);
    }

    @Nullable
    public Map<String, Object> queryOne(SelectQuery<Record> query) {
        List<Map<String, Object>> maps = executeQuery(query);
        if (maps.size() == 0)
        {
            return null;
        }

        return maps.get(0);
    }

    @Nullable
    public <T> T queryOne(Class<T> c, String sql, Object... args) {
        ensureModelClass(c);
        try {
            Map<String, Object> map = getJdbcTemplate().queryForMap(sql, args);
            return Core.M().convertMapToModel(map, c);
        } catch (EmptyResultDataAccessException ignored) { }
        return null;
    }

    public List querySingleColumnList(String column, String sql, Object... args) {
        List<Map<String, Object>> resultList = getJdbcTemplate().queryForList(sql, args);
        List list = new ArrayList();
        if (resultList != null && resultList.size() > 0) {
            resultList.forEach((Map<String, Object> record) -> {
                if (!record.containsKey(column)) {
                    throw new BaseException(-1, "record doesn't contains col");
                }
                list.add(record.get(column));
            });
        }
        return list;
    }

    public List<Map<String, Object>> query(SelectQuery<Record> query) {
        return executeQuery(query);
    }

    public <T> List<T> query(Class<T> c, SelectQuery<Record> query) {
        ensureModelClass(c);
        List<Map<String, Object>> maps = executeQuery(query);
        return processQueryResultMaps(c, maps);
    }

    public <T> List<T> query(Class<T> c, String sql, Object... args) {
        ensureModelClass(c);
        List<Map<String, Object>> maps = getJdbcTemplate().queryForList(sql, args);
        return processQueryResultMaps(c, maps);
    }

    public List<Map<String, Object>> query(String sql, Object... args) {
        return getJdbcTemplate().queryForList(sql, args);
    }

    public int update(String sql, Object... args) {
        return getJdbcTemplate().update(sql, args);
    }



    public Map<String, Object> getRawById(Class<?> modelClass, Long id) {
        return getRawByCol(modelClass, "id", id);
    }

    public Map<String, Object> getRawOrFail(Class<?> modelClass, Long id) {
        Map<String, Object> map = getRawById(modelClass, id);
        if (map == null) {
            throw new NotExistsException();
        }

        return map;
    }

    public Map<String, Object> getRawByCol(Class<?> modelClass, String colName, Object colValue) {
        DSLContext dsl = getDSLContext();

        SelectQuery query = dsl.select()
                .from(getTableName(modelClass))
                .where(field(colName).eq(colValue)).getQuery();

        List<Map<String, Object>> maps = executeQuery(query);
        if (maps.size() == 0)
        {
            return null;
        }

        return maps.get(0);
    }
}
