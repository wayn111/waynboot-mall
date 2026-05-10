# 库存一致性改造说明

## 背景

原订单链路在下单阶段直接扣减 `shop_goods_product.number`，取消和退款再统一 `addStock` 回补。该模型能通过 `number >= #{number}` 避免基础超卖，但无法区分“未支付冻结库存”和“已支付确认库存”，本地消息重试时也缺少库存维度的幂等流水。

本轮按“ MySQL 条件更新 + 冻结库存 + 本地消息补偿 + 库存流水”收口库存一致性，目标是在不引入订单事件表的前提下，让订单库存副作用可追踪、可补偿、可幂等。

## 改造范围

- `shop_goods_product` 新增 `locked_stock`，`number` 继续表示可售库存。
- 新增 `shop_inventory_flow` 记录冻结、确认、释放、退款回补库存流水。
- 下单责任链从“直接扣库存”改为“冻结库存”。
- 未支付订单取消/超时关闭从“直接加库存”改为“释放冻结库存”。
- 支付成功后置本地消息新增冻结库存确认，再更新虚拟销量。
- 管理端退款成功仍按现有业务回补可售库存，并补充库存流水。

## 核心链路

```text
下单事务
-> 写库存冻结流水
-> update shop_goods_product
   set number = number - n, locked_stock = locked_stock + n
   where id = ? and number >= n
-> 订单主表 / 订单商品 / 优惠券 / 延迟关单本地消息同事务提交

支付回调事务
-> 订单待支付状态条件更新为已支付
-> 写 ORDER_PAID_POST_ACTION 本地消息
-> relay 调用本地处理器
-> 写库存确认流水
-> update shop_goods_product
   set locked_stock = locked_stock - n
   where id = ? and locked_stock >= n
-> 更新商品虚拟销量

取消/超时关闭事务
-> 订单待支付状态条件更新为关闭
-> 写库存释放流水
-> update shop_goods_product
   set number = number + n, locked_stock = locked_stock - n
   where id = ? and locked_stock >= n
-> 回退优惠券
```

## 并发与一致性

- MySQL 条件更新是库存并发控制的最终防线，内存库存校验只用于返回更友好的商品库存不足信息。
- `shop_inventory_flow.flow_key` 唯一，支付确认、取消释放、退款回补在本地消息重试或重复补偿时先命中流水幂等，再决定是否执行库存更新。
- 下单冻结流水和库存冻结更新处于同一个下单事务，订单落库失败会一起回滚，避免孤立冻结。
- 支付确认通过现有 `ORDER_PAID_POST_ACTION` 本地消息补偿，不新增 MQ 队列，避免链路继续复杂化。

## 表结构

脚本位于 `db-init/inventory_stock.sql`：

- `shop_goods_product.locked_stock`：冻结库存数量。
- `shop_inventory_flow`：库存流水表。
- `uk_inventory_flow_key`：库存副作用幂等键。
- `idx_inventory_flow_biz`：按订单维度排查库存流水。
- `idx_inventory_flow_product`：按 SKU 和变更类型排查库存变更。

## 非目标

- 本轮不引入 Redis 预扣库存，库存强一致性仍以 MySQL 为准。
- 本轮不改变“下单冻结库存、支付确认库存”的业务口径。
- 本轮不拆分支付成功虚拟销量的独立本地消息，后续可继续把库存确认和销量更新解耦。

## 验证方式

已覆盖测试：

- `GoodsProductServiceImplTest`
- `InventoryFlowServiceTest`
- `OrderStockSupportTest`
- `PaymentPostActionSupportTest`
- `OrderCancellationSupportTest`

推荐命令：

```bash
mvn "-Dmaven.repo.local=.m2/repository" -pl waynboot-common -am "-Dtest=OrderStockSupportTest,GoodsProductServiceImplTest,InventoryFlowServiceTest,PaymentPostActionSupportTest,OrderCancellationSupportTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn "-Dmaven.repo.local=.m2/repository" test
```

## 后续计划

- 支付成功后置动作可拆成库存确认消息和销量更新消息，进一步降低单条本地消息处理失败后的重复副作用风险。
- 库存流水可补充变更前后库存快照，便于后台排查和审计。
- 后续高并发活动场景可在 MySQL 强一致库存外增加 Redis 热点库存读写削峰，但必须保留 MySQL 条件更新兜底。

## 全链路关系

本库存改造是完整交易方法论中的库存一致性环节，和 `ECOMMERCE_TRADE_SYSTEM_METHODOLOGY.md` 的关系如下：

- 下单链路：当前项目已落地 MySQL 条件冻结库存，后续 Redis Lua 预扣只作为入口削峰。
- 支付链路：支付成功通过本地消息确认冻结库存，避免支付回调同步执行过多副作用。
- 状态链路：取消订单必须先通过订单状态条件更新，再释放冻结库存，避免支付成功和超时取消并发下重复回补。
- 补偿链路：库存流水唯一键是库存副作用幂等依据，后续对账和人工补偿都应优先围绕库存流水展开。
