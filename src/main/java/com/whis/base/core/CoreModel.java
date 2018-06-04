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
import java.lang.reflect.Method;
import java.util.*;

import static org.jooq.impl.DSL.field;

/**
 * Created by dd on 21/07/2017.
 */
public class CoreModel {

    private static Logger logger = LoggerFactory.getLogger(CoreModel.class);

    private static Map<String, String[]> modelKeysCache = new HashMap<>();
    private static Map<String, BaseModel> modelCache = new HashMap<>();

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

    public Model getModel(Class<?> modelClass) {
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

    public boolean isBaseModel(Class cls) {
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

    protected BaseModel getBaseModel(Class cls) {
        String key = cls.getCanonicalName();
        if (modelCache.containsKey(key)) {
            return modelCache.get(key);
        }

        Object instance;
        try {
            instance = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + cls);
        }

        if (!(instance instanceof BaseModel)) {
            throw new BaseException(-1, "get model fail, not BaseModel");
        }

        return (BaseModel) instance;
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
    public <T> T safeConvertMapToModel(Map<String, Object> map, Class<T> clazz) {
        checkModelClass(clazz);

        if (map == null || map.isEmpty()) {
            return null;
        }
        String[] keys = getModelKeys(clazz);

        Object instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + clazz);
        }

        BaseModel baseModel = (BaseModel) instance;
        for (String key : keys) {
            if (!map.containsKey(key)) {
                continue;
            }
            baseModel.set(key, map.get(key));
        }

        return (T) instance;
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

    @Nullable
    public Map<String, Object> convertModelToMap(BaseModel model) {
        return convertModelToMap(model, model.keys());
    }

    @Nullable
    public Map<String, Object> convertModelToMap(BaseModel model, String[] keys) {
        if (model == null) {
            return null;
        }

        if (keys == null) {
            keys = model.keys();
        }

        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        for (String key : keys) {
            resultMap.put(key, model.get(key));
        }

        return resultMap;
    }

    // model may be Map or BaseModel
    @Nullable
    public Map invokeProcessModel(Object model, Class<?> modelClass, Object invoker, String[] keys)
    {
        if (model == null || modelClass == null) {
            return null;
        }

        Method processModelMethod = null;
        Object[] methodArgs = null;
        try {
            if (keys == null) {
                keys = getModelKeys(modelClass);
                if (keys == null) {
                    return null;
                }
            }

            String methodName = null;
            if (invoker instanceof String) {
                methodName = (String) invoker;
            } else if (invoker instanceof Invoker) {
                Invoker ivk = (Invoker) invoker;
                methodName = ivk.getName();
                methodArgs = ivk.getParams();
            } else if (invoker != null) {
                throw new BaseException(-1, "invalid invoker");
            }

            if (methodName == null) {
                methodName = "process";
            }

            if (methodArgs == null) {
                processModelMethod = modelClass.getMethod(methodName, Object.class, String[].class);
            } else {
                processModelMethod = modelClass.getMethod(methodName, Object.class, String[].class, Object[].class);
            }

            if (processModelMethod == null) {
                return null;
            }

            BaseModel modelInstance = getBaseModel(modelClass);
            Object processModelResult = null;
            if (methodArgs == null) {
                processModelResult = processModelMethod.invoke(modelInstance, model, keys);
            } else {
                processModelResult = processModelMethod.invoke(modelInstance, model, keys, methodArgs);
            }

            if (processModelResult instanceof Map) {
                return (Map) processModelResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // model may be Map or BaseModel
    @Nullable
    public Map<String, Object> processModel(Object model, String[] keys, Class<?> modelClass) {
        if (model == null) {
            return null;
        }

        if (keys == null) {
            if (model instanceof BaseModel) {
                keys = ((BaseModel) model).keys();
            }
            if (modelClass != null) {
                checkModelClass(modelClass);
                keys = getModelKeys(modelClass);
            }
        }

        if (keys == null || keys.length == 0) {
            return null;
        }

        Map map = null;
        if (model instanceof Map) {
            map = (Map) model;
        } else if (model instanceof BaseModel) {
            map = convertModelToMap((BaseModel) model);
        }

        if (map == null || map.isEmpty()) {
            return null;
        }

        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
        for (String key : keys) {
            if (map.containsKey(key)) {
                resultMap.put(key, map.get(key));
            }
        }

        return resultMap;
    }

    public List<Map> processModelList(List modelList, Class modelClass)
    {
        return processModelList(modelList, modelClass, null, null);
    }

    public List<Map> processModelList(List modelList, Class modelClass, Object invoker)
    {
        return processModelList(modelList, modelClass, invoker, null);
    }

    public List<Map> processModelList(List modelList, Class<?> modelClass, Object invoker, String[] keys)
    {
        ArrayList<Map> resultList = new ArrayList<Map>();
        for (Object model : modelList) {
            Map map = invokeProcessModel(model, modelClass, invoker, keys);
            if (map != null) {
                resultList.add(map);
            }
        }

        return resultList;
    }

    public List<Map> processModelList(List modelList, ProcessModelInterface processModelInterface, String[] keys)
    {
        ArrayList<Map> resultList = new ArrayList<Map>();
        for (Object model : modelList) {
            Map map = processModelInterface.processModel(model, keys);
            if (map != null) {
                resultList.add(map);
            }
        }

        return  resultList;
    }

    public List<Map> processModelList(List modelList, ProcessModelWithArgsInterface processModelWithArgsInterface, Object[] args, String[] keys)
    {
        ArrayList<Map> resultList = new ArrayList<Map>();
        for (Object model : modelList) {
            Map map = processModelWithArgsInterface.processModel(model, keys, args);
            if (map != null) {
                resultList.add(map);
            }
        }

        return  resultList;
    }

}
