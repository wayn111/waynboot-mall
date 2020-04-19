package com.wayn.common.annotation;

import com.wayn.common.enums.Operator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    /**
     * 模块名称
     *
     * @return
     */
    String value() default "";

    /**
     * 操作类型
     *
     * @return
     */
    Operator operator() default Operator.SELECT;

    /**
     * 是否记录请求参数
     *
     * @return
     */
    boolean isNeedParam() default true;
}
