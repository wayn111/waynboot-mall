# 完整电商交易系统方法论

## 背景

本项目已经具备 MQ 异步下单、订单状态机、本地消息表、冻结库存、库存流水、支付回调幂等等核心能力。为了让后续维护和面试表达都能从单点优化升级为完整交易链路，本文件把“下单 → 库存 → 支付 → 状态机 → MQ → 分账 → 对账 → 分表 → 高可用”的设计口径统一沉淀下来。

注意：日订单量 `10w+` 并不等于系统只需要支撑平均 TPS。真正的复杂度来自活动峰值、热点商品、支付回调洪峰、MQ 堆积、数据库热点行和跨服务最终一致性。

## 当前项目能力映射

| 能力点 | 当前状态 | 代码或文档落点 |
| --- | --- | --- |
| MQ 异步下单 | 已具备 | `OrderSubmitMessageSupport`、`OrderPayConsumer` |
| 本地消息表 | 已具备 | `LocalMessageService`、`LocalMessageRelaySupport`、`db-init/local_message.sql` |
| 冻结库存 | 已具备 | `OrderStockSupport`、`GoodsProductMapper.xml` |
| 库存流水 | 已具备 | `InventoryFlowService`、`db-init/inventory_stock.sql` |
| 订单状态机 | 已具备 | `OrderStateTransitionSupport` |
| 支付回调幂等 | 已具备 | `PaymentCallbackSupport` 的状态条件更新 |
| MQ 消费幂等 | 已具备 | `MessageConsumerSupport`、消费模板测试 |
| 商品详情缓存 | 已具备 | `GoodsDetailServiceImpl`、库存变更后缓存失效 |
| Redis Lua 预扣 | 待增强 | 后续活动峰值专项 |
| 订单分表 | 待增强 | 后续数据规模治理专项 |
| 分账与对账 | 待增强 | 后续支付中台专项 |
| 监控告警 | 待增强 | 后续高可用治理专项 |

## 总体架构口径

完整交易链路建议按下面的职责分层：

```text
入口层
-> Gateway 限流 / 风控 / 参数校验
-> 移动端或管理端 Controller

交易编排层
-> 幂等控制
-> 下单责任链
-> 订单状态机
-> 支付回调统一入口

领域能力层
-> 库存冻结 / 确认 / 释放
-> 优惠券占用 / 回退
-> 订单商品快照
-> 支付流水 / 退款流水

异步一致性层
-> 本地消息表
-> MQ relay
-> 消费端幂等
-> XXL-Job 补偿

治理层
-> 分表分库
-> 对账任务
-> 指标监控
-> 限流降级
```

核心原则是：同步链路只保留订单核心落库、库存冻结和必要幂等校验；权益发放、通知、搜索同步、分账、对账这类可补偿副作用统一异步化。

## 10w 订单量下的下单链路

推荐链路：

```text
用户下单
-> Gateway 限流
-> Redis 幂等校验
-> Redis Lua 原子预扣库存
-> 写订单预创建记录或本地消息
-> MQ 异步削峰
-> 消费者创建正式订单
-> MySQL 条件冻结库存
-> 订单状态流转
-> 支付成功
-> 本地消息确认库存 / MQ 发权益 / 分账 / 通知
```

当前项目已经落地了“本地消息 + MQ + MySQL 条件冻结库存 + 状态机”，后续如果要支撑活动峰值，需要继续补 Redis Lua 预扣和热点库存削峰。

## 幂等设计

重复下单来源包括用户连点、网络重试、App 重试、网关重试和 MQ 重复消费。推荐幂等键：

```text
order:req:{userId}:{requestNo}
```

执行方式：

```text
SET key value NX EX 15
```

订单落库和 MQ 消费仍需要数据库级幂等，例如订单号唯一索引、本地消息 `message_key` 唯一、库存流水 `flow_key` 唯一。Redis 幂等用于前置挡流，不能替代数据库最终一致性约束。

## 库存模型

交易库存不应只有一个 `stock` 字段。推荐三态模型：

```text
available_stock 可售库存
locked_stock    冻结库存
sold_stock      已售库存
```

当前项目中：

```text
number       = available_stock
locked_stock = locked_stock
sold_stock   = 后续可按销售统计或库存流水聚合补齐
```

状态变化：

```text
下单冻结: available - n, locked + n
支付确认: locked - n, sold + n
超时取消: locked - n, available + n
退款回补: available + n
```

当前项目已通过 `GoodsProductMapper.freezeStock/releaseFrozenStock/confirmFrozenStock` 使用 MySQL 条件更新保证高并发下不超卖。

## 订单状态机

订单状态必须通过状态机和数据库条件更新共同控制。典型状态：

```text
WAIT_PAY
PAID
CANCEL
REFUND
FINISH
```

支付成功：

```sql
UPDATE shop_order
SET order_status = 'PAID'
WHERE order_sn = ?
  AND order_status = 'WAIT_PAY';
```

超时取消：

```sql
UPDATE shop_order
SET order_status = 'CANCEL'
WHERE order_sn = ?
  AND order_status = 'WAIT_PAY';
```

支付回调和超时取消并发时，谁先条件更新成功谁生效。失败的一方必须重新读取订单状态并走幂等返回，不能强行覆盖状态。

## MQ 与本地消息表

同步下单不应串行完成发权益、通知、分账、ES 同步和日志写入。正确做法是：

```text
业务事务
-> 写订单 / 库存 / 支付状态
-> 写 local_message
-> 事务提交
-> Relay 扫描 INIT 消息
-> 投递 MQ 或执行本地处理器
-> 成功标记 SENT
-> 失败重试或进入 FAILED
```

本地消息表解决的是“业务提交成功但 MQ 发送失败”的问题。消费端仍然必须基于订单号、业务类型、消息 ID 或流水键做幂等，不能只依赖 MQ 至少一次投递。

## 支付回调

支付回调的核心要求：

- 验签必须先于业务处理。
- 支付流水号或渠道流水号必须唯一。
- 订单状态更新必须带当前状态条件。
- 回调重复通知要返回成功幂等结果。
- 支付成功后的副作用必须异步化并可补偿。

当前项目已经在 `PaymentCallbackSupport` 中用 `STATUS_CREATE -> STATUS_PAY` 条件更新防止重复支付回调，并通过 `ORDER_PAID_POST_ACTION` 本地消息处理库存确认和虚拟销量。

## 分账与对账

分账不建议放在支付回调同步主链路中。推荐：

```text
支付成功
-> 写分账待处理记录
-> 本地消息 / MQ 异步分账
-> 渠道返回分账结果
-> 更新分账流水
-> 定时任务补偿失败或超时分账
```

对账需要按天对齐：

```text
订单支付金额
VS 支付渠道账单
VS 支付流水表
VS 退款流水表
VS 分账流水表
```

库存对账需要对齐：

```text
订单商品数量
VS 库存流水
VS SKU 当前库存
```

异常结果进入对账差异表，后续由自动补偿或人工处理。

## 分表与索引

日订单量 `10w+` 时，一年约 `3650w`，三年接近 `1 亿`。订单表需要提前设计分表策略。

推荐按月分表：

```text
shop_order_202601
shop_order_202602
shop_order_goods_202601
shop_order_goods_202602
```

核心索引：

```sql
UNIQUE KEY uk_order_sn(order_sn);
KEY idx_user_create_time(user_id, create_time);
KEY idx_status_create_time(order_status, create_time);
```

按月分表适合订单天然按时间查询的场景。用户订单列表必须带时间范围或分页游标，避免跨大量历史表扫全量。

## 热点治理

活动峰值下真正危险的是热点商品和热点库存行。可选治理方案：

- Redis Lua 预扣库存，用 Redis 扛入口流量，MySQL 做最终确认。
- 热点商品缓存预热，避免大量详情请求直接穿透数据库。
- 库存分段桶，例如把一个 SKU 库存拆成多个库存段，降低单行锁竞争。
- MQ 异步削峰，消费者批量拉取并控制数据库写入速率。
- 降级策略，库存系统异常时只允许查询，不允许创建新交易。

本项目当前优先保留 MySQL 条件更新兜底，后续引入 Redis 预扣时必须处理 Redis 预扣成功但订单创建失败后的库存回补。

## 监控指标

交易链路至少需要覆盖：

- Gateway：QPS、RT、限流次数、拒绝率。
- Redis：命中率、热 Key、慢查询、连接数。
- MQ：堆积量、消费延迟、死信数量、重试次数。
- DB：TPS、慢 SQL、锁等待、连接池耗尽。
- 订单：下单成功率、支付成功率、取消率、退款率。
- 本地消息：INIT 数量、FAILED 数量、重试次数、最大滞留时间。

监控目标不是只看系统活着，而是能在 MQ 堆积、库存锁等待、支付回调失败这类交易风险发生前提前告警。

## 面试回答口径

如果被问“10w 订单量下怎么设计订单系统”，可以按下面顺序回答：

```text
先说明难点不是平均 TPS，而是活动峰值、热点商品、支付回调洪峰和跨服务一致性。
再讲入口限流、Redis 幂等、Redis Lua 预扣、MQ 削峰和 MySQL 条件确认。
然后讲库存三态模型、订单状态机和支付/取消并发下的 CAS 更新。
接着讲本地消息表 + MQ + XXL-Job 补偿解决最终一致性。
最后讲分表索引、对账、监控和高可用降级。
```

推荐完整回答：

```text
我们订单系统日订单量 10w+，核心难点不是平均 TPS，而是活动期间的热点商品、支付回调洪峰、库存竞争和跨服务一致性问题。

下单链路上采用 Gateway 限流、Redis 幂等键、Redis Lua 原子预扣库存、MQ 异步削峰和 MySQL 条件更新确认库存。库存模型拆成可售库存、冻结库存和已售库存，下单冻结库存，支付成功确认库存，超时未支付释放库存。

订单系统通过订单状态机控制 WAIT_PAY、PAID、CANCEL、REFUND、FINISH 等状态流转。支付成功和超时取消都使用带当前状态条件的 SQL 更新，谁先更新成功谁生效，失败方按幂等结果返回。

支付成功后的库存确认、权益发放、分账、通知和搜索同步不放在同步主链路里，而是通过本地消息表、MQ 和补偿任务保证最终一致性。消费端基于订单号、业务类型和流水键做幂等，避免重复发货或重复扣库存。

数据层面订单主表、明细表、支付流水表、状态日志表拆分设计，订单号使用雪花算法，核心索引围绕 order_no、user_id + create_time、status + create_time 建立，并按月分表控制单表规模。

最后围绕 Gateway、Redis、MQ、DB、本地消息和支付回调建立监控，包括 QPS、RT、错误率、MQ 堆积、Redis 热 Key、慢 SQL、锁等待和消息失败数，保障核心交易链路稳定运行。
```

## 后续演进顺序

建议按风险收益排序推进：

1. 支付流水表和支付回调流水幂等，补齐渠道流水唯一约束。
2. 订单状态日志表，记录每次状态流转来源、操作者和失败原因。
3. Redis Lua 预扣库存，用于活动峰值削峰。
4. 库存流水补充变更前后库存快照，增强审计和对账能力。
5. 本地消息后台管理和人工重投能力。
6. 支付分账流水、退款流水和日终对账任务。
7. 订单按月分表和历史订单归档。
8. Gateway 限流和热点商品保护策略。
