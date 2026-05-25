package com.wayn.admin.framework.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * ShedLock 分布式定时任务锁配置。
 * 使用 Redis 作为锁存储，保证 admin 多实例部署时交易治理任务只有一个实例执行。
 */
@Configuration
public class ShedLockConfig {

    /**
     * 创建 ShedLock Redis 锁提供器。
     *
     * @param connectionFactory Redis 连接工厂
     * @return ShedLock 锁提供器
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "waynboot-mall");
    }
}
