package com.wayn.common.util.bean;

import org.springframework.beans.BeanUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * bean帮助类
 */
public class MyBeanUtil extends BeanUtils {
    /**
     * 将map中的值拷贝到bean中
     *
     * @param source 属性来源map
     * @param target 目标Bean
     */
    public static void copyProperties2Bean(Map<String, Object> source, Object target) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor prop : properties) {
            String key = prop.getName();
            if (source.containsKey(key) && source.get(key) != null) {
                Object value = source.get(key);
                Method setMethod = prop.getWriteMethod();
                setMethod.invoke(target, value);
            }
        }


    }

    /**
     * 将bean中的属性存入到map中
     *
     * @param source 来源bean
     * @param target 目标map
     */
    public static void copyProperties2Map(Object source, Map<String, Object> target) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        //1.获取bean信息
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        if (properties != null && properties.length > 0) {
            for (PropertyDescriptor prop : properties) {
                //2.得到属性名
                String name = prop.getName();
                //3.过滤class属性
                if (!"class".equals(name)) {
                    //4.得到属性的get方法
                    Method getter = prop.getReadMethod();

                    //5.获取属性值
                    Object value = getter.invoke(source);
                    //6.放入map中
                    if (value != null) {
                        target.put(name, value);
                    }
                }
            }
        }
    }


    /**
     * 根据字段名从对象反射取值
     *
     * @param source
     * @param filed
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object getValue(Object source, String filed) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        //1.获取bean信息
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

        if (properties != null && properties.length > 0) {
            for (PropertyDescriptor prop : properties) {
                //2.得到属性名
                String name = prop.getName();
                //3.过滤class属性
                if (!"class".equals(name) && Objects.equals(filed, name)) {
                    //4.得到属性的get方法
                    Method getter = prop.getReadMethod();

                    //5.获取属性值
                    Object value = getter.invoke(source);
                    return value;

                }
            }
        }
        return null;
    }

}

