CREATE TABLE IF NOT EXISTS `shop_payment_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `flow_key` varchar(128) NOT NULL COMMENT '支付流水幂等键，格式为 payChannel:payId',
  `order_id` bigint NOT NULL COMMENT '订单 ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单号',
  `pay_id` varchar(128) NOT NULL COMMENT '第三方支付流水号',
  `pay_channel` varchar(32) NOT NULL COMMENT '支付回调渠道编码',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint NOT NULL COMMENT '支付流水状态：1 支付成功',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_flow_key` (`flow_key`),
  KEY `idx_payment_flow_order_sn` (`order_sn`),
  KEY `idx_payment_flow_pay_id` (`pay_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付流水表';

CREATE TABLE IF NOT EXISTS `shop_payment_channel_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `bill_date` date NOT NULL COMMENT '渠道账单日期',
  `order_sn` varchar(64) NOT NULL COMMENT '订单号',
  `pay_id` varchar(128) NOT NULL COMMENT '第三方支付流水号',
  `pay_channel` varchar(32) NOT NULL COMMENT '支付渠道编码',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '渠道账单支付金额',
  `bill_status` varchar(32) DEFAULT NULL COMMENT '渠道账单状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_channel_bill` (`pay_channel`, `pay_id`),
  KEY `idx_payment_channel_bill_date` (`bill_date`),
  KEY `idx_payment_channel_bill_order` (`order_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付渠道账单表';

CREATE TABLE IF NOT EXISTS `shop_payment_refund_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `refund_key` varchar(128) NOT NULL COMMENT '退款流水幂等键',
  `order_id` bigint DEFAULT NULL COMMENT '订单 ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单号',
  `refund_id` varchar(128) NOT NULL COMMENT '第三方退款流水号',
  `refund_channel` varchar(32) NOT NULL COMMENT '退款渠道编码',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `status` tinyint NOT NULL COMMENT '退款状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_refund_flow_key` (`refund_key`),
  KEY `idx_payment_refund_flow_order` (`order_sn`),
  KEY `idx_payment_refund_flow_refund` (`refund_channel`, `refund_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付退款流水表';
