package com.wayn.mobile.framework.manager.thread;


import com.wayn.common.util.ThreadUtil;
import com.wayn.common.util.spring.SpringContextUtil;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器
 *
 * @author ruoyi
 */
public class AsyncManager {
    private static AsyncManager me = new AsyncManager();
    /**
     * 异步操作任务调度线程池
     */
    private ScheduledExecutorService executor = SpringContextUtil.getBean("scheduledExecutorService");

    /**
     * 单例模式
     */
    private AsyncManager() {
    }

    public static AsyncManager me() {
        return me;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(TimerTask task) {
        int OPERATE_DELAY_TIME = 10;
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param task 任务
     * @param delay 延迟时间
     * @param unit  是加你单位
     */
    public void execute(TimerTask task, long delay, TimeUnit unit) {
        executor.schedule(task, delay, unit);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown() {
        ThreadUtil.shutdownAndAwaitTermination(executor);
    }
}
