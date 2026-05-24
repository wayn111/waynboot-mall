package com.wayn.domain.trade.outbox;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 本地消息 relay 定时任务。
 * 周期性扫描到期待投递消息，避免业务事务提交后因为 MQ 短暂不可用导致异步副作用丢失。
 */
@Component
@AllArgsConstructor
public class LocalMessageRelayTask {

    private static final int DEFAULT_BATCH_SIZE = 50;

    private final LocalMessageRelaySupport localMessageRelaySupport;

    /**
     * 执行本地消息投递。
     */
    @Scheduled(fixedDelay = 5000L)
    public void relayPendingMessages() {
        localMessageRelaySupport.relayPendingMessages(DEFAULT_BATCH_SIZE);
    }
}
