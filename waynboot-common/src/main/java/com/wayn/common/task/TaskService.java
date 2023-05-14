package com.wayn.common.task;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
@Component
public class TaskService {
    private final DelayQueue<Task> delayQueue = new DelayQueue<>();

    @PostConstruct
    private void init() {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build());
        executorService.execute(() -> {
            while (true) {
                try {
                    Task task = delayQueue.take();
                    task.run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public boolean addTask(Task task) {
        if (delayQueue.contains(task)) {
            return false;
        }
        return delayQueue.add(task);
    }

    public boolean removeTask(Task task) {
        return delayQueue.remove(task);
    }

}
