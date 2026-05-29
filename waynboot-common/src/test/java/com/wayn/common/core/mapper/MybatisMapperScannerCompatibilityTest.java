package com.wayn.common.core.mapper;

import com.wayn.common.core.mapper.system.DeptMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class MybatisMapperScannerCompatibilityTest {

    @Test
    void mapperFactoryBeanObjectTypeUsesClassAttribute() {
        GenericApplicationContext context = new GenericApplicationContext();
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(context);
        scanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
        scanner.registerFilters();

        scanner.scan(DeptMapper.class.getPackageName());

        BeanDefinition deptMapper = context.getBeanDefinition("deptMapper");
        assertThat(deptMapper.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE))
                .isEqualTo(DeptMapper.class);
    }
}
