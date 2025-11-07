package com.wayn.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key
     */
    String key() default "rate_limit:";

    /**
     * 限流时间，单位秒
     */
    int time() default 60;

    /**
     * 限流次数
     */
    int count() default 100;

    /**
     * 限流类型
     */
    LimitType type() default LimitType.IP;

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * IP限流
         */
        IP,
        /**
         * 用户限流
         */
        USER,
        /**
         * 接口限流
         */
        INTERFACE
    }
}
