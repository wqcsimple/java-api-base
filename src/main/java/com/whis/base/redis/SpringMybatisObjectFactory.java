package com.whis.base.redis;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by dd on 7/28/16.
 */
public class SpringMybatisObjectFactory extends DefaultObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringMybatisObjectFactory.class);

    private static ApplicationContext context;

    public static void setContext(ApplicationContext context)
    {
        SpringMybatisObjectFactory.context = context;
    }

    private boolean isSpringBean(Class type)
    {
        return type.isAnnotationPresent(Component.class);
    }

    public <T> T create(Class<T> type)
    {
        T bean = null;

        if (isSpringBean(type))
        {
            try {
                bean = context.getBean(type);
                logger.debug("create bean: " + type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (bean == null)
        {
            bean = super.create(type);
        }

        return bean;
    }

    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs)
    {
        T bean = null;

        if (isSpringBean(type))
        {
            try {
                bean = context.getBean(type, constructorArgs);
                logger.debug("create bean: " + type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (bean == null)
        {
            bean = super.create(type, constructorArgTypes, constructorArgs);
        }

        return bean;
    }
}
