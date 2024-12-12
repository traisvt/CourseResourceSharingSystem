package com.kit.sharingsystem.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;

@Configuration
public class MyBatisConfig {
    @Bean
    public TypeHandler<java.time.LocalDateTime> localDateTimeTypeHandler() {
        return new LocalDateTimeTypeHandler();
    }
}
