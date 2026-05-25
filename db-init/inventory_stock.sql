CREATE TABLE IF NOT EXISTS `shop_inventory_flow` (
                                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
                                                     `flow_key` varchar(128) NOT NULL COMMENT '业务唯一流水键',
                                                     `biz_type` varchar(32) NOT NULL COMMENT '业务类型',
                                                     `biz_id` varchar(64) NOT NULL COMMENT '业务 ID',
                                                     `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
                                                     `product_id` bigint NOT NULL COMMENT '商品货品 ID',
                                                     `change_type` varchar(32) NOT NULL COMMENT '库存变更类型',
                                                     `change_number` int NOT NULL COMMENT '库存变更数量',
                                                     `remark` varchar(255) DEFAULT NULL COMMENT '流水备注',
                                                     `create_time` datetime NOT NULL COMMENT '创建时间',
                                                     `update_time` datetime NOT NULL COMMENT '更新时间',
                                                     PRIMARY KEY (`id`),
                                                     UNIQUE KEY `uk_inventory_flow_key` (`flow_key`),
                                                     KEY `idx_inventory_flow_biz` (`biz_type`, `biz_id`),
                                                     KEY `idx_inventory_flow_product` (`product_id`, `change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存流水表';
