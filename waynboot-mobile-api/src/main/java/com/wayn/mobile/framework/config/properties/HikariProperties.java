package com.wayn.mobile.framework.config.properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HikariProperties {

    @Value("${spring.datasource.master.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.master.username}")
    private String username;
    @Value("${spring.datasource.master.password}")
    private String password;


    @Value("${spring.datasource.hikari.poolName}")
    private String poolName;

    @Value("${spring.datasource.hikari.minimumIdle}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.maximumPoolSize}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.autoCommit}")
    private boolean autoCommit;

    @Value("${spring.datasource.hikari.idleTimeout}")
    private int idleTimeout;

    @Value("${spring.datasource.hikari.maxLifetime}")
    private int maxLifetime;

    @Value("${spring.datasource.hikari.connectionTimeout}")
    private int connectionTimeout;

    @Value("${spring.datasource.hikari.connectionTestQuery}")
    private String connectionTestQuery;

    @Value("${spring.datasource.hikari.cachePrepStmts}")
    private boolean cachePrepStmts;
    @Value("${spring.datasource.hikari.prepStmtCacheSize}")
    private int prepStmtCacheSize;
    @Value("${spring.datasource.hikari.prepStmtCacheSqlLimit}")
    private int prepStmtCacheSqlLimit;
    @Value("${spring.datasource.hikari.useServerPrepStmts}")
    private boolean useServerPrepStmts;
    @Value("${spring.datasource.hikari.useLocalSessionState}")
    private boolean useLocalSessionState;
    @Value("${spring.datasource.hikari.rewriteBatchedStatements}")
    private boolean rewriteBatchedStatements;
    @Value("${spring.datasource.hikari.cacheResultSetMetadata}")
    private boolean cacheResultSetMetadata;
    @Value("${spring.datasource.hikari.elideSetAutoCommits}")
    private boolean elideSetAutoCommits;
    @Value("${spring.datasource.hikari.maintainTimeStats}")
    private boolean maintainTimeStats;

    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.setPoolName(poolName);
        config.setMinimumIdle(minimumIdle);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setAutoCommit(autoCommit);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connectionTimeout);
        config.setConnectionTestQuery(connectionTestQuery);
        config.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
        config.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);
        config.addDataSourceProperty("useServerPrepStmts", useServerPrepStmts);
        config.addDataSourceProperty("useLocalSessionState", useLocalSessionState);
        config.addDataSourceProperty("rewriteBatchedStatements", rewriteBatchedStatements);
        config.addDataSourceProperty("cacheResultSetMetadata", cacheResultSetMetadata);
        config.addDataSourceProperty("elideSetAutoCommits", elideSetAutoCommits);
        config.addDataSourceProperty("maintainTimeStats", maintainTimeStats);

        return new HikariDataSource(config);
    }
}
