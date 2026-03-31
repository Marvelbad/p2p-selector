package com.cibit.p2p.selector.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class OracleDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource oracleDataSource() {
        return oracleDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * JdbcTemplate для вызова хранимых процедур Oracle.
     * Таймаут 30 секунд защищает от зависания если Oracle не отвечает.
     */
    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource oracleDataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(oracleDataSource);
        jdbcTemplate.setQueryTimeout(30);
        return jdbcTemplate;
    }

}