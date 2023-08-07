package com.wayn.data.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LettuceConfig implements InitializingBean {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void afterPropertiesSet() {
        if (redisConnectionFactory instanceof LettuceConnectionFactory c) {
            c.setValidateConnection(true);
        }
    }
}
