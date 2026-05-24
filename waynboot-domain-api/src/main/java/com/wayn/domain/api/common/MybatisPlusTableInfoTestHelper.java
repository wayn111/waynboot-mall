package com.wayn.domain.api.common;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

/**
 * 纯单测环境下初始化 MyBatis-Plus 实体元数据，避免 lambdaWrapper 因未加载 Mapper 而抛出缓存缺失异常。
 */
public final class MybatisPlusTableInfoTestHelper {

    private static final MapperBuilderAssistant ASSISTANT =
            new MapperBuilderAssistant(new MybatisConfiguration(), "test");

    private MybatisPlusTableInfoTestHelper() {
    }

    /**
     * 初始化指定实体的表元数据。
     *
     * @param entityClasses 实体类型列表
     */
    public static void init(Class<?>... entityClasses) {
        for (Class<?> entityClass : entityClasses) {
            if (TableInfoHelper.getTableInfo(entityClass) == null) {
                TableInfoHelper.initTableInfo(ASSISTANT, entityClass);
            }
        }
    }
}
