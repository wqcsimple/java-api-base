package com.whis.app;


import com.google.common.collect.Lists;
import com.whis.app.common.Config;
import com.whis.base.core.CoreConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.whis.app", "com.whis.base"})
@MapperScan(basePackages = {"com.whis.app.mapper"})
public class Application {

    public static void main(String[] args) throws Exception {

        CoreConfig.getInstance()
                .getGuestCanAccessPathPatternList()
                .add(Lists.newArrayList(Config.PATH_GUEST_CAN_ACCESS_PATTERN));

        CoreConfig.getInstance()
                .getDefaultInterceptorExcludePathList()
                .add(Lists.newArrayList(Config.DEFAULT_INTERCEPTOR_EXCLUDE_PATH_PATTERN));

        CoreConfig.getInstance().getDebug().set(true);

        SpringApplication.run(Application.class, args);
    }
}
