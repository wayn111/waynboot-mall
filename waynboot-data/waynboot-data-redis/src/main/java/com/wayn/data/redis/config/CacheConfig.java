package com.wayn.data.redis.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.wayn.data.redis.constant.CacheConstants;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceCustomizer() {

        return builder -> {
            if (SystemUtils.IS_OS_LINUX) {
                // create your socket options
                SocketOptions socketOptions = SocketOptions.builder()
                        .tcpUserTimeout(SocketOptions.TcpUserTimeoutOptions.builder()
                                .enable(true)
                                .tcpUserTimeout(Duration.ofSeconds(15))
                                .build()
                        )
                        .keepAlive(SocketOptions.KeepAliveOptions.builder()
                                .enable()
                                .idle(Duration.ofSeconds(30))
                                .interval(Duration.ofSeconds(10))
                                .count(3)
                                .build()
                        ).build();
                builder.clientOptions(ClientOptions.builder()
                        .socketOptions(socketOptions)
                        .build())
                ;
            }
        };
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer() {
        return new GenericFastJsonRedisSerializer();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return new MyRedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory), defaultCacheConfig());
    }

    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()))
                .entryTtl(Duration.ofSeconds(600))
                .computePrefixWith(cacheName -> CacheConstants.CACHE_PREFIX + cacheName)
                .disableCachingNullValues();
    }
}
