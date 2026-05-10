package com.wayn.common.core.service.shop;

/**
 * 库存流水创建命令。
 * 调用方只传入业务语义字段，创建时间、更新时间和数据库主键由 InventoryFlowService 统一填充。
 *
 * @param flowKey 业务唯一流水键
 * @param bizType 业务类型
 * @param bizId 业务 ID
 * @param goodsId 商品 ID
 * @param productId 商品货品 ID
 * @param changeType 库存变更类型
 * @param changeNumber 库存变更数量
 * @param remark 流水备注
 */
public record InventoryFlowCreateCommand(String flowKey, String bizType, String bizId, Long goodsId,
                                         Long productId, String changeType, Integer changeNumber, String remark) {

    /**
     * 创建库存流水命令构造器。
     *
     * @return 构造器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 库存流水命令构造器。
     */
    public static final class Builder {

        private String flowKey;
        private String bizType;
        private String bizId;
        private Long goodsId;
        private Long productId;
        private String changeType;
        private Integer changeNumber;
        private String remark;

        /**
         * 设置业务唯一流水键。
         *
         * @param flowKey 业务唯一流水键
         * @return 构造器
         */
        public Builder flowKey(String flowKey) {
            this.flowKey = flowKey;
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
         * 设置商品 ID。
         *
         * @param goodsId 商品 ID
         * @return 构造器
         */
        public Builder goodsId(Long goodsId) {
            this.goodsId = goodsId;
            return this;
        }

        /**
         * 设置商品货品 ID。
         *
         * @param productId 商品货品 ID
         * @return 构造器
         */
        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        /**
         * 设置库存变更类型。
         *
         * @param changeType 库存变更类型
         * @return 构造器
         */
        public Builder changeType(String changeType) {
            this.changeType = changeType;
            return this;
        }

        /**
         * 设置库存变更数量。
         *
         * @param changeNumber 库存变更数量
         * @return 构造器
         */
        public Builder changeNumber(Integer changeNumber) {
            this.changeNumber = changeNumber;
            return this;
        }

        /**
         * 设置流水备注。
         *
         * @param remark 流水备注
         * @return 构造器
         */
        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        /**
         * 构建库存流水创建命令。
         *
         * @return 库存流水创建命令
         */
        public InventoryFlowCreateCommand build() {
            return new InventoryFlowCreateCommand(flowKey, bizType, bizId, goodsId, productId, changeType,
                    changeNumber, remark);
        }
    }
}
