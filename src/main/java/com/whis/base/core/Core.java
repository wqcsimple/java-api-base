package com.whis.base.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Created by dd on 7/24/16.
 */
public class Core {

    private static final Logger logger = LoggerFactory.getLogger(Core.class);

    private static ApplicationContext context;
    private static DataSource dataSource;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void setContext(ApplicationContext context)
    {
        Core.context = context;
    }

    public static boolean isSpringContextReady() {
        return Core.context != null;
    }

    public static void setDataSource(DataSource dataSource)
    {
        Core.dataSource = dataSource;
    }

    public static DataSource getDataSource()
    {
        return Core.dataSource;
    }

    private static boolean isSpringBean(Class type)
    {
        return true;
        // return type.isAnnotationPresent(Component.class);
    }

    @Nullable
    public static <T> T getBean(Class<T> type)
    {
        T bean = null;
        if (isSpringBean(type))
        {
            try {
                bean = context.getBean(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bean;
    }

    @Nullable
    public static <T> T getBean(String name, Class<T> requiredType)
    {
        T bean = null;
        if (isSpringBean(requiredType))
        {
            try {
                bean = context.getBean(name, requiredType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bean;
    }

    @Nullable
    public static Object getBean(String name)
    {
        Object bean = null;
        try {
            bean = context.getBean(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bean;
    }

    public static Resource getResource(String location) {
        return context.getResource(location);
    }


    @Nullable
    public static Map<String, Object> processModel(Object model) {
        return CoreModel.getInstance().processModel(model, null, null);
    }

    @Nullable
    public static Map<String, Object> processModel(Object model, Class<?> modelClass) {
        return CoreModel.getInstance().processModel(model, null, modelClass);
    }

    @Nullable
    public static Map<String, Object> processModel(Object model, String[] keys) {
        return CoreModel.getInstance().processModel(model, keys, null);
    }

    public static List<Map> processModelList(List modelList, Class modelClass)
    {
        return CoreModel.getInstance().processModelList(modelList, modelClass, null, null);
    }

    public static List<Map> processModelList(List modelList, Class modelClass, String methodName)
    {
        return CoreModel.getInstance().processModelList(modelList, modelClass, methodName, null);
    }

    public static List<Map> processModelList(List modelList, Class modelClass, Invoker invoker)
    {
        return CoreModel.getInstance().processModelList(modelList, modelClass, invoker, null);
    }

    public static List<Map> processModelList(List modelList, Class<?> modelClass, String invoker, String[] keys)
    {
        return CoreModel.getInstance().processModelList(modelList, modelClass, invoker, keys);
    }

    public static List<Map> processModelList(List modelList, Class<?> modelClass, Invoker invoker, String[] keys)
    {
        return CoreModel.getInstance().processModelList(modelList, modelClass, invoker, keys);
    }

    public static List<Map> processModelList(List modelList, ProcessModelInterface processModelInterface, String[] keys)
    {
        return CoreModel.getInstance().processModelList(modelList, processModelInterface, keys);
    }

    public static List<Map> processModelList(List modelList, ProcessModelWithArgsInterface processModelWithArgsInterface, Object[] args, String[] keys)
    {
        return CoreModel.getInstance().processModelList(modelList, processModelWithArgsInterface, args, keys);
    }

    public static CoreQuery Q()
    {
        return CoreQuery.getInstance();
    }
    public static CoreModel M() { return CoreModel.getInstance(); }
}
