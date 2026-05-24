package com.wayn.domain.trade.outbox;

/**
 * 本地消息创建命令。
 * 业务侧只传递消息语义和投递目标，状态、重试和时间字段由 LocalMessageService 统一填充。
 *
 * @param messageKey 业务唯一消息键
 * @param topic 消息主题
 * @param bizType 业务类型
 * @param bizId 业务 ID
 * @param exchangeName RabbitMQ 交换机，空值表示本地处理器消息
 * @param routingKey RabbitMQ 路由键，空值表示本地处理器消息
 * @param payload JSON 消息体
 * @param delayMillis 延迟投递毫秒数
 */
public record LocalMessageCreateCommand(String messageKey, String topic, String bizType, String bizId,
                                        String exchangeName, String routingKey, String payload,
                                        Integer delayMillis) {

    /**
     * 创建命令构造器。
     *
     * @return 构造器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 本地消息创建命令构造器。
     */
    public static final class Builder {

        private String messageKey;
        private String topic;
        private String bizType;
        private String bizId;
        private String exchangeName;
        private String routingKey;
        private String payload;
        private Integer delayMillis;

        /**
         * 设置业务唯一消息键。
         *
         * @param messageKey 业务唯一消息键
         * @return 构造器
         */
        public Builder messageKey(String messageKey) {
            this.messageKey = messageKey;
            return this;
        }

        /**
         * 设置消息主题。
         *
         * @param topic 消息主题
         * @return 构造器
         */
        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        /**
         * 设置业务类型。
         *
         * @param bizType 业务类型
         * @return 构造器
         */
        public Builder bizType(String bizType) {
            this.bizType = bizType;
            return this;
        }

        /**
         * 设置业务 ID。
         *
         * @param bizId 业务 ID
         * @return 构造器
         */
        public Builder bizId(String bizId) {
            this.bizId = bizId;
            return this;
        }

        /**
         * 设置 RabbitMQ 交换机。
         *
         * @param exchangeName RabbitMQ 交换机
         * @return 构造器
         */
        public Builder exchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        /**
         * 设置 RabbitMQ 路由键。
         *
         * @param routingKey RabbitMQ 路由键
         * @return 构造器
         */
        public Builder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        /**
         * 设置 JSON 消息体。
         *
         * @param payload JSON 消息体
         * @return 构造器
         */
        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        /**
         * 设置延迟投递毫秒数。
         *
         * @param delayMillis 延迟投递毫秒数
         * @return 构造器
         */
        public Builder delayMillis(Integer delayMillis) {
            this.delayMillis = delayMillis;
            return this;
        }

        /**
         * 构建本地消息创建命令。
         *
         * @return 本地消息创建命令
         */
        public LocalMessageCreateCommand build() {
            return new LocalMessageCreateCommand(messageKey, topic, bizType, bizId, exchangeName, routingKey,
                    payload, delayMillis);
        }
    }
}
