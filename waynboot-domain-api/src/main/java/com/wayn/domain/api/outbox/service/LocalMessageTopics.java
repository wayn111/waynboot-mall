package com.wayn.domain.api.outbox.service;

/**
 * 本地消息主题常量。
 * 统一定义业务写入和 relay/handler 识别使用的主题，避免硬编码分散在订单链路中。
 */
public final class LocalMessageTopics {

    public static final String BIZ_TYPE_ORDER = "ORDER";

    public static final String ORDER_SUBMIT = "ORDER_SUBMIT";

    public static final String ORDER_UNPAID_DELAY = "ORDER_UNPAID_DELAY";

    public static final String ORDER_PAID_POST_ACTION = "ORDER_PAID_POST_ACTION";

    /**
     * 工具类禁止实例化。
     */
    private LocalMessageTopics() {
    }
}
