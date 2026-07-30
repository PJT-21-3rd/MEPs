package org.meps.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


@Configuration
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
@MapperScan("org.meps.mapper")
@ComponentScan(
        basePackages = "org.meps",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = Controller.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = Configuration.class
                )
        }
)

public class RootConfig {

    @Value("${jdbc.driver}")
    private String driver;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Value("${hikari.maximumPoolSize}")
    private int maximumPoolSize;

    @Value("${hikari.minimumIdle}")
    private int minimumIdle;

    @Value("${hikari.connectionTimeout}")
    private long connectionTimeout;

    @Bean
    public DataSource dataSource() {

        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driver
        );

        config.setJdbcUrl(
                url
        );

        config.setUsername(
                username
        );

        config.setPassword(
                password
        );

        config.setMaximumPoolSize(
                maximumPoolSize
        );

        config.setMinimumIdle(
                minimumIdle
        );

        config.setConnectionTimeout(
                connectionTimeout
        );

        config.setPoolName(
                "MepsHikariPool"
        );

        return new HikariDataSource(
                config
        );
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(
            DataSource dataSource
    ) throws Exception {

        SqlSessionFactoryBean factory =
                new SqlSessionFactoryBean();

        factory.setDataSource(
                dataSource
        );

        factory.setTypeAliasesPackage(
                "org.meps.domain"
        );

        Resource[] mapperLocations =
                new PathMatchingResourcePatternResolver()
                        .getResources(
                                "classpath:/org/meps/mapper/*.xml"
                        );

        factory.setMapperLocations(
                mapperLocations
        );

        org.apache.ibatis.session.Configuration
                configuration =
                new org.apache.ibatis.session.Configuration();

        configuration.setMapUnderscoreToCamelCase(
                true
        );

        factory.setConfiguration(
                configuration
        );

        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            DataSource dataSource
    ) {

        return new DataSourceTransactionManager(
                dataSource
        );
    }

    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper mapper =
                new ObjectMapper();

        mapper.registerModule(
                new JavaTimeModule()
        );

        mapper.disable(
                SerializationFeature
                        .WRITE_DATES_AS_TIMESTAMPS
        );

        return mapper;
    }
}
