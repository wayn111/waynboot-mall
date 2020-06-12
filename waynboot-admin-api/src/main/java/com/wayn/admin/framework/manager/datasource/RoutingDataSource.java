package com.wayn.admin.framework.manager.datasource;

import com.wayn.admin.framework.manager.datasource.thread.DataSourceHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 数据源切换类，<br>
 * 根据determineCurrentLookupKey方法返回key值，切换到对应的数据源
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    public RoutingDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceHolder.get();
    }
}
