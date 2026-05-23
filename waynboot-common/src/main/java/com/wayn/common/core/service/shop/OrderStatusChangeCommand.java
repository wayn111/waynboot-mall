package com.wayn.common.core.service.shop;

import com.wayn.common.core.enums.OrderStatusChangeTypeEnum;
import com.wayn.util.enums.OrderStatusEnum;

/**
 * 订单状态日志创建命令。
 * 编排层只传入订单状态流转语义，日志实体字段、成功失败标记和时间由 OrderStatusLogService 统一填充。
 *
 * @param orderId 订单 ID
 * @param orderSn 订单号
 * @param sourceStatus 来源状态
 * @param targetStatus 目标状态
 * @param changeType 状态变更类型
 * @param operatorType 操作者类型
 * @param operatorId 操作者 ID 或渠道标识
 * @param remark 备注
 */
public record OrderStatusChangeCommand(Long orderId, String orderSn, OrderStatusEnum sourceStatus,
                                       OrderStatusEnum targetStatus, OrderStatusChangeTypeEnum changeType,
                                       String operatorType, String operatorId, String remark) {

    /**
     * 创建订单状态日志命令构造器。
     *
     * @return 构造器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 订单状态日志命令构造器。
     */
    public static final class Builder {

        private Long orderId;
        private String orderSn;
        private OrderStatusEnum sourceStatus;
        private OrderStatusEnum targetStatus;
        private OrderStatusChangeTypeEnum changeType;
        private String operatorType;
        private String operatorId;
        private String remark;

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
         * 设置来源状态。
         *
         * @param sourceStatus 来源状态
         * @return 构造器
         */
        public Builder sourceStatus(OrderStatusEnum sourceStatus) {
            this.sourceStatus = sourceStatus;
            return this;
        }

        /**
         * 设置目标状态。
         *
         * @param targetStatus 目标状态
         * @return 构造器
         */
        public Builder targetStatus(OrderStatusEnum targetStatus) {
            this.targetStatus = targetStatus;
            return this;
        }

        /**
         * 设置状态变更类型。
         *
         * @param changeType 状态变更类型
         * @return 构造器
         */
        public Builder changeType(OrderStatusChangeTypeEnum changeType) {
            this.changeType = changeType;
            return this;
        }

        /**
         * 设置操作者类型。
         *
         * @param operatorType 操作者类型
         * @return 构造器
         */
        public Builder operatorType(String operatorType) {
            this.operatorType = operatorType;
            return this;
        }

        /**
         * 设置操作者 ID 或渠道标识。
         *
         * @param operatorId 操作者 ID 或渠道标识
         * @return 构造器
         */
        public Builder operatorId(String operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        /**
         * 设置备注。
         *
         * @param remark 备注
         * @return 构造器
         */
        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        /**
         * 构建订单状态日志创建命令。
         *
         * @return 订单状态日志创建命令
         */
        public OrderStatusChangeCommand build() {
            return new OrderStatusChangeCommand(orderId, orderSn, sourceStatus, targetStatus, changeType,
                    operatorType, operatorId, remark);
        }
    }
}
