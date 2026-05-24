package com.wayn.domain.api.outbox.enums;

import lombok.Getter;

/**
 * 本地消息状态枚举。
 * 状态只描述本地消息 relay 的处理进度，不替代业务订单状态。
 */
@Getter
public enum LocalMessageStatusEnum {

    /**
     * 待投递或待执行。
     */
    INIT((short) 0),

    /**
     * 已成功投递或处理。
     */
    SENT((short) 1),

    /**
     * 超过最大重试次数后的失败状态。
     */
    FAILED((short) 2);

    private final Short status;

    /**
     * 构造本地消息状态。
     *
     * @param status 状态值
     */
    LocalMessageStatusEnum(Short status) {
        this.status = status;
    }
}
