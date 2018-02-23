package com.whis.base.configuration;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
public class DataSourceConfig {

    @Value("${spring.jdbc-pool.driver-class-name}")
    private String driverClassName;

    @Value("${spring.jdbc-pool.url}")
    private String url;

    @Value("${spring.jdbc-pool.username}")
    private String username;

    @Value("${spring.jdbc-pool.password}")
    private String password;

    @Value("${spring.jpa.database}")
    private String database;

    @Value("${spring.jdbc-pool.initial-size}")
    private Integer initialSize;

    @Value("${spring.jdbc-pool.max-active}")
    private Integer maxActive;

    @Value("${spring.jdbc-pool.min-idle}")
    private Integer minIdle;

    @Value("${spring.jdbc-pool.max-idle}")
    private Integer maxIdle;

    @Value("${spring.jdbc-pool.remove-abandoned}")
    private boolean removeAbandoned;

    @Value("${spring.jdbc-pool.remove-abandoned-timeout}")
    private Integer removeAbandonedTimeout;

    @Bean
    @Primary
    @ConfigurationProperties(prefix="datasource.mysql")
    public DataSource dataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setRemoveAbandoned(removeAbandoned);
        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        dataSource.setValidationQuery("select 1");
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setRemoveAbandoned(true);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Primary
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}