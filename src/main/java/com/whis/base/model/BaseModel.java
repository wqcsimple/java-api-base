package com.whis.base.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.whis.base.common.ModelDiff;
import com.whis.base.common.Util;
import com.whis.base.core.Core;
import com.whis.base.core.CoreQuery;
import com.whis.base.exception.BaseException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dd on 21/07/2017.
 */
public abstract class BaseModel {

    private static Logger logger = LoggerFactory.getLogger(BaseModel.class);
    private static Map<String, Method[]> methodCache = new HashMap<>();
    private static Map<String, Method[]> keyMethodCache = new HashMap<>();

    public abstract Long ID();

    private Map<String, Object> initialAttributes = new HashMap<>();

    @JsonIgnore
    public abstract String[] keys();

    @JsonIgnore
    public abstract String[] basicKeys();

    @JsonIgnore
    public abstract String[] detailKeys();

    private Method[] getMethods(Class cls) {
        String key = cls.getCanonicalName();
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        }

        Method[] methods = cls.getMethods();
        methodCache.put(key, methods);
        return methods;
    }

    public void set(String key, Object value)
    {
        invokeSet(this, key, value);
    }

    private void setInitialAttribute(String key, Object value) {
        if (initialAttributes.containsKey(key)) {
            return;
        }
        initialAttributes.put(key, value);
    }

    private Method searchMethod(String methodName, int parameterCount) {
        for (Method method : getMethods(this.getClass()))
        {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount)
            {
                return method;
            }
        }
        return null;
    }

    private String generateMethodName(String prefix, String key) {
        return generateMethodName(prefix, key, false);
    }

    // is_master -> setMaster / isMaster
    // user_id -> setUserId / getUserId
    private String generateMethodName(String prefix, String key, boolean compatibleWithKotlin) {
        String[] keyParts = StringUtils.split(key, "_");
        if (prefix.equals("get") && compatibleWithKotlin) {
            prefix = "is";
        }
        StringBuilder methodName = new StringBuilder(prefix);
        for (String keyPart : keyParts) {
            if (compatibleWithKotlin && keyPart.equals("is")) {
                continue;
            }
            methodName.append(StringUtils.capitalize(keyPart));
        }
        return methodName.toString();
    }

    public void invokeSet(Object target, String key, Object value)
    {
        String methodName = generateMethodName("set", key);
        String alternativeMethodName = "";
        Method method = searchMethod(methodName, 1);
        if (method == null) {
            // fix compatibility with kotlin setter: is_default -> getDefault, not getIsDefault
            alternativeMethodName = generateMethodName("set", key, true);
            method = searchMethod(alternativeMethodName, 1);
        }
        if (method == null) {
            logger.error("set fail, can not find method: {} {}, {} {}", target, key, methodName, alternativeMethodName);
            return;
        }

        // logger.trace("set methodName: {}, value: {} / {}, {} / {}", methodName, value, value.getClass(), methodName, alternativeMethodName);

        Class<?> valueType = method.getParameterTypes()[0];
        if (value != null && valueType != value.getClass()
                && ClassUtils.primitiveToWrapper(valueType) != ClassUtils.primitiveToWrapper(value.getClass())
                && !valueType.isAssignableFrom(value.getClass())) {
            // logger.warn("key {} {} type mismatch: {} <-> {}", target.getClass(), key, value.getClass(), valueType);
            // return;
        }

        try {
            value = ConvertUtils.convert(value, valueType);
            method.invoke(target, value);
            setInitialAttribute(key, value);
            return;
        } catch (IllegalAccessException e) {
            logger.error("invoke {}.{} throws IllegalAccessException: {}", this.getClass(), methodName, e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("invoke {}.{} throws InvocationTargetException: {}", this.getClass(), methodName, e.getMessage());
        }

        logger.error("set fail: {} {} {}", target, key, value);
    }

    public Object get(String key)
    {
        return invokeGet(this, key);
    }

    public Object invokeGet(Object target, String key)
    {
        String methodName = generateMethodName("get", key);
        String alternativeMethodName = "";

        // logger.trace("get methodName: {}, {}", methodName, alternativeMethodName);

        Method method = searchMethod(methodName, 0);
        if (method == null) {
            // fix compatibility with kotlin getter: is_default -> isDefault, not getIsDefault
            alternativeMethodName = generateMethodName("get", key, true);
            method = searchMethod(alternativeMethodName, 0);
        }

        if (method == null) {
            logger.error("get fail, can not find method: {} {}, {} {}", target, key, methodName, alternativeMethodName);
            return null;
        }

        try {
            return method.invoke(target);
        } catch (IllegalAccessException e) {
            logger.error("invoke {}.{} throws IllegalAccessException: {}", this.getClass(), methodName, e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("invoke {}.{} throws InvocationTargetException: {}", this.getClass(), methodName, e.getMessage());
        }

        logger.error("invoke get fail: {} {}, {} {}", target, key, methodName, alternativeMethodName);

        return null;
    }

    public boolean beforeSave(boolean setTime)
    {
        Long time = Util.time();
        if (isNew() && setTime)
        {
            set("create_time", time);
        }

        if (setTime)
        {
            set("update_time", time);
        }

        return true;
    }

    public void afterSave()
    {
        Model model = this.getClass().getAnnotation(Model.class);
        if (model.enableCache()) {
            cache();
        }
    }

    public void cache() {
//        Redis redis = Core.getBean(Redis.class);
//        redis.cache(this);
    }

    public int save() {
        return persist();
    }

    public void updateCol(String colName, Object colValue)
    {
        Core.Q().updateCol(this.getClass(), this.ID(), colName, colValue);
    }

    public void fetch()
    {
        Object latestModel = Core.Q().findById(this.getClass(), this.ID());
        if (latestModel == null) {
            return;
        }

        String[] keys = keys();
        for (String key : keys)
        {
            logger.info("fetch set: {} -> {}", key, invokeGet(latestModel, key));
            set(key, invokeGet(latestModel, key));
        }
    }

    public int persist() {
        Model model = getClass().getAnnotation(Model.class);
        if (model == null) {
            throw new BaseException(-1, "" + getClass().toString() + " must have annotation: Model");
        }

        if (!beforeSave(true)) {
            throw new BaseException(-1, getClass().toString() + " beforeSave return false");
        }

        int count = 0;
        if (this.isNew()) {
            count = CoreQuery.getInstance().insert(this);
        } else {
            count = CoreQuery.getInstance().update(this);
        }

        afterSave();

        return count;
    }

    @JsonIgnore
    public boolean isNew()
    {
        return this.ID() == null || this.ID() == 0;
    }

    public Map<String, Object> process() {
        return this.process(this, basicKeys());
    }

    public Map<String, Object> process(String[] keys) {
        return this.process(this, keys);
    }

    public Map<String, Object> process(Object model) {
        return this.process(model, basicKeys());
    }

    public Map<String, Object> process(Object model, String[] keys) {
        return Core.processModel(model, keys);
    }

    @JsonIgnore
    public Map<String, ModelDiff> getDirtyAttributes() {
        Map<String, ModelDiff> changedMap = new HashMap<>();
        for(String key : keys()) {
            Object old = initialAttributes.get(key);
            Object current = get(key);
            if (current != null && !current.equals(old)) {
                changedMap.put(key, new ModelDiff(old, current));
            }
        }
        return changedMap;
    }
}
