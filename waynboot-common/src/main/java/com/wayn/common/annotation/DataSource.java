package com.wayn.common.annotation;

import com.wayn.common.enums.DataSourceEnum;

import java.lang.annotation.*;

/**
 * 数据源注解
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    DataSourceEnum value() default DataSourceEnum.MASTER;
}
