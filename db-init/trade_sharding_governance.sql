-- 交易链路分表与查询治理建议脚本。
-- 当前代码先沉淀按月物理表命名规则，正式启用前需要结合 ShardingSphere 或路由层执行建表。

-- 订单主表月分表命名：shop_order_yyyyMM
-- 核心查询：订单详情、用户订单列表、待支付扫描。
-- 推荐索引：
--   UNIQUE KEY uk_shop_order_order_sn (order_sn)
--   KEY idx_shop_order_user_time (user_id, create_time)
--   KEY idx_shop_order_status_time (order_status, create_time)

-- 订单明细表月分表命名：shop_order_goods_yyyyMM
-- 核心查询：按 order_id 查询订单明细。
-- 推荐索引：
--   KEY idx_shop_order_goods_order_id (order_id)
--   KEY idx_shop_order_goods_product_id (product_id)

-- 支付流水月分表命名：shop_payment_flow_yyyyMM
-- 核心查询：按 order_sn / pay_channel + pay_id 对账和幂等。
-- 推荐索引：
--   UNIQUE KEY uk_payment_flow_key (flow_key)
--   KEY idx_payment_flow_order_sn (order_sn)
--   KEY idx_payment_flow_pay_id (pay_channel, pay_id)

-- 本地消息月分表命名：local_message_yyyyMM
-- 核心查询：relay 扫描、业务补偿、死信查看。
-- 推荐索引：
--   UNIQUE KEY uk_local_message_key (message_key)
--   KEY idx_local_message_status_retry (status, next_retry_time)
--   KEY idx_local_message_biz (biz_type, biz_id)
