package com.wayn.common.core.service.shop;

import java.math.BigDecimal;

/**
 * 支付流水创建命令。
 * 支付回调支撑服务只传入渠道和订单语义字段，状态和时间字段由 PaymentFlowService 统一填充。
 *
 * @param flowKey 支付流水幂等键
 * @param orderId 订单 ID
 * @param orderSn 订单号
 * @param payId 第三方支付流水号
 * @param payChannel 支付回调渠道编码
 * @param payAmount 支付金额
 */
public record PaymentFlowCreateCommand(String flowKey, Long orderId, String orderSn, String payId,
                                       String payChannel, BigDecimal payAmount) {

    /**
     * 创建支付流水命令构造器。
     *
     * @return 构造器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 支付流水命令构造器。
     */
    public static final class Builder {

        private String flowKey;
        private Long orderId;
        private String orderSn;
        private String payId;
        private String payChannel;
        private BigDecimal payAmount;

        /**
         * 设置支付流水幂等键。
         *
         * @param flowKey 支付流水幂等键
         * @return 构造器
         */
        public Builder flowKey(String flowKey) {
            this.flowKey = flowKey;
            return this;
        }

        /**
         * 设置订单 ID。
         *
         * @param orderId 订单 ID
         * @return 构造器
         */
        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        /**
         * 设置订单号。
         *
         * @param orderSn 订单号
         * @return 构造器
         */
        public Builder orderSn(String orderSn) {
            this.orderSn = orderSn;
            return this;
        }

        /**
         * 设置第三方支付流水号。
         *
         * @param payId 第三方支付流水号
         * @return 构造器
         */
        public Builder payId(String payId) {
            this.payId = payId;
            return this;
        }

        /**
         * 设置支付回调渠道编码。
         *
         * @param payChannel 支付回调渠道编码
         * @return 构造器
         */
        public Builder payChannel(String payChannel) {
            this.payChannel = payChannel;
            return this;
        }

        /**
         * 设置支付金额。
         *
         * @param payAmount 支付金额
         * @return 构造器
         */
        public Builder payAmount(BigDecimal payAmount) {
            this.payAmount = payAmount;
            return this;
        }

        /**
         * 构建支付流水创建命令。
         *
         * @return 支付流水创建命令
         */
        public PaymentFlowCreateCommand build() {
            return new PaymentFlowCreateCommand(flowKey, orderId, orderSn, payId, payChannel, payAmount);
        }
    }
}
