package com.wayn.common.config;

import com.wayn.common.task.ThreadPoolExecutorMdcWrapper;
import com.wayn.common.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置，各系统分开使用
 **/
@Slf4j
@Configuration
public class ThreadPoolConfig {
    // 核心线程池大小
    private final int corePoolSize = 5;

    // 最大可创建的线程数
    private final int maxPoolSize = 10;

    // 队列最大长度
    private final int queueCapacity = 2000;

    // 线程池维护线程所允许的空闲时间
    private final int keepAliveSeconds = 300;

    @Bean(name = "commonThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor homeThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = getThreadPoolTaskExecutor();
        BasicThreadFactory build = new BasicThreadFactory.Builder()
                .namingPattern("home-task-%d")
                .uncaughtExceptionHandler((t, e) -> log.error("commonTaskExecutor:{},error:{}", t.getName(), e.getMessage(), e))
                .build();
        executor.setThreadFactory(build);
        executor.initialize();
        return executor;
    }

    @NotNull
    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolExecutorMdcWrapper();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(corePoolSize, new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ThreadUtil.printException(r, t);
            }
        };
    }
}
