package com.wayn.mobile.framework.config;

import com.wayn.mobile.framework.config.properties.HikariProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class HikariCpConfig {


    @Bean
    public DataSource dataSource(HikariProperties hikariProperties) {
        return hikariProperties.dataSource();
    }
}
