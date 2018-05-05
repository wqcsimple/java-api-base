package com.whis.base.core;

import com.google.common.collect.Lists;
import com.whis.base.exception.BaseException;
import com.whis.base.model.BaseModel;
import com.whis.base.model.Model;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.field;

/**
 * Created by dd on 21/07/2017.
 */
public class CoreModel {

    private static Logger logger = LoggerFactory.getLogger(CoreModel.class);

    private static Map<String, String[]> modelKeysCache = new HashMap<>();

    private static Configuration configuration = new DefaultConfiguration();

    private static volatile CoreModel instance;

    public static CoreModel getInstance() {
        if (instance == null) {
            synchronized (CoreModel.class) {
                if (instance == null) {
                    instance = new CoreModel();
                }
            }
        }
        return instance;
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

    private Field[] getFields(BaseModel model) {
        String[] keys = model.keys();
        Field[] fields = new Field[keys.length];
        for (int i = 0; i < keys.length; i++) {
            fields[i] = field(keys[i]);
        }
        return fields;
    }

    public boolean isModelClass(Class cls) {
        return BaseModel.class.isAssignableFrom(cls);
    }

    protected String[] getModelKeys(Class cls) {
        String key = cls.getCanonicalName();
        if (modelKeysCache.containsKey(key)) {
            return modelKeysCache.get(key);
        }

        Object instance;
        try {
            instance = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + cls);
        }

        if (!(instance instanceof BaseModel)) {
            throw new BaseException(-1, "get model keys fail, not BaseModel");
        }

        BaseModel baseModel = (BaseModel) instance;
        String[] keys = baseModel.keys();
        modelKeysCache.put(key, keys);
        return keys;
    }

    public String[] keys(BaseModel model) {
        return Lists.newArrayList(model.keys()).stream().filter(item -> !item.equals("id")).toArray(String[]::new);
    }

    public Map<String, Object> ensureModelMap(Map<String, Object> map, String[] keys) {
        Map<String, Object> model = new HashMap<String, Object>();
        for (String key : keys) {
            if (!map.containsKey(key)) {
                throw new BaseException(-1, "map missing required key: " + key);
            }
            model.put(key, map.get(key));
        }

        return model;
    }

    public <T> void checkModelClass(Class<T> clazz) {
        if (!BaseModel.class.isAssignableFrom(clazz)) {
            throw new BaseException(-1, clazz + " is not BaseModel");
        }
    }

    @Nullable
    public <T> T convertMapToModel(Map<String, Object> map, Class<T> clazz) {
        checkModelClass(clazz);

        if (map == null || map.isEmpty()) {
            return null;
        }
        String[] keys = getModelKeys(clazz);
        Map<String, Object> filteredMap = ensureModelMap(map, keys);

        Object instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + clazz);
        }

        BaseModel baseModel = (BaseModel) instance;
        for (String key : keys) {
            baseModel.set(key, filteredMap.get(key));
        }

        return (T) instance;
    }

    public <T> List<T> convertMapToModel(List<Map<String, Object>> maps, Class<T> clazz) {
        return convertMapListToModelList(maps, clazz);
    }

    public <T> List<T> convertMapListToModelList(List<Map<String, Object>> maps, Class<T> clazz) {
        checkModelClass(clazz);

        List<T> resultList = new ArrayList<>();

        if (maps == null || maps.isEmpty()) {
            return resultList;
        }

        for (Map<String, Object> map : maps) {
            resultList.add(convertMapToModel(map, clazz));
        }

        return resultList;
    }

}
