package com.wayn.common.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: waynaqua
 * @date: 2023/11/6 23:53
 */
@Component
public class RedisKeyGenConfig {

    @Bean("cacheKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            if (params.length == 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder("_");
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Page<?> page) {
                    long current = page.getCurrent();
                    long size = page.getSize();
                    sb.append("page").append("_").append(current).append("_").append(size);
                } else {
                    sb.append(param.toString());
                }
                if (i < params.length - 1) {
                    sb.append("_");
                }
            }
            return sb.toString();
        };
    }
}
