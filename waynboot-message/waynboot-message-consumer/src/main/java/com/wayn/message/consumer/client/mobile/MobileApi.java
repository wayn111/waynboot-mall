package com.wayn.message.consumer.client.mobile;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

/**
 * mobile服务调用api
 *
 * @author: waynaqua
 * @date: 2023/8/20 18:30
 */
public interface MobileApi {
    void submitOrder(String body) throws Exception;

    /**
     * 批量调用 mobile 下单回调接口。
     *
     * @param bodies MQ 原始消息体列表
     * @throws Exception 调用失败或回调返回失败时抛出
     */
    void submitOrders(List<String> bodies) throws Exception;

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    void unpaidOrder(String body) throws Exception;

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    void sendEmail(String body) throws Exception;
}
