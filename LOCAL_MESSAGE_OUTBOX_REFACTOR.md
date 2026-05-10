# 本地消息表最终一致性改造说明

## 背景

本轮不引入订单事件表，统一使用通用本地消息表承载异步副作用。业务事务先写业务数据和 `local_message`，再由 relay 定时扫描投递 RabbitMQ 或执行本地处理器，避免业务提交成功但 MQ 或后置动作丢失。

## 范围

- 异步下单消息：入口层生成订单号后写入本地消息表，由 relay 投递 `ORDER_DIRECT_EXCHANGE`。
- 未支付延迟关单消息：订单落库事务内写入本地消息表，由 relay 投递延迟队列。
- 支付成功后置动作：支付状态更新和本地消息同事务提交，由本地处理器确认冻结库存并更新虚拟销量。
- 本地消息基础设施：新增实体、Mapper、Service、Relay、定时任务和状态枚举。

## 核心链路

```text
业务事务
-> 更新业务表
-> 写 local_message
-> 事务提交
-> LocalMessageRelayTask 扫描 INIT 且到期消息
-> 投递 RabbitMQ 或调用 LocalMessageHandler
-> 成功标记 SENT
-> 失败累计 retry_count 并计算 next_retry_time
```

## 表结构

建表脚本位于 `db-init/local_message.sql`。关键约束：

- `message_key` 唯一，保障业务侧重复写入时幂等。
- `status + next_retry_time` 索引用于 relay 扫描到期消息。
- `biz_type + biz_id` 索引用于后续按业务对象排查消息。

## 并发与一致性

- 下单消息不再直接依赖 RabbitMQ 可用性，入口只负责写本地消息。
- 延迟关单消息与订单落库同事务，订单回滚时不会产生孤儿关单消息。
- 支付成功后置动作从线程池异步改成本地消息重试，冻结库存确认和虚拟销量更新失败可继续重试。
- 库存确认通过 `shop_inventory_flow.flow_key` 保证幂等，本地消息重试不会重复扣减冻结库存。
- 消费端幂等仍然保留，本地消息表只保证可靠投递，不替代消费者幂等。

## 验证方式

已覆盖测试：

- `LocalMessageServiceTest`
- `LocalMessageRelaySupportTest`
- `OrderSubmitMessageSupportTest`
- `PaymentPostActionSupportTest`
- `PaymentCallbackSupportTest`
- `OrderStockSupportTest`
- `InventoryFlowServiceTest`

推荐命令：

```bash
mvn "-Dmaven.repo.local=.m2/repository" test
```

## 后续计划

- 增加本地消息后台管理查询接口，便于人工排查失败消息。
- 对 `FAILED` 状态增加人工重投入口。
- 增加 relay 指标：待投递数、成功数、失败数、重试次数。
- 后续商品 ES 同步、通知、积分等异步副作用统一接入本地消息表。
