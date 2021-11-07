package com.wayn.admin.api.service.impl;

import com.anji.captcha.service.CaptchaCacheService;
import com.wayn.data.redis.manager.RedisCache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService {

    @Autowired
    private RedisCache redisCache;

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        redisCache.setCacheObject(key, value, (int) expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return redisCache.existsKey(key);
    }

    @Override
    public void delete(String key) {
        redisCache.deleteObject(key);
    }

    @Override
    public String get(String key) {
        return redisCache.getCacheObject(key);
    }

    @Override
    public String type() {
        return "redis";
    }
}
