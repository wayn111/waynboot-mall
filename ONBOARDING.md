# 新手上手指南

本文档面向第一次接触 waynboot-mall 的开发者，帮助你在 30 分钟内建立对代码结构的整体认知，并能独立追踪一条完整的业务链路。

---

## 一、先看这几个文件

按顺序阅读，每个文件都不超过 120 行：

| 顺序 | 文件 | 读完你会明白 |
|---|---|---|
| 1 | `waynboot-util/.../enums/OrderStatusEnum.java` | 订单状态码的含义和分组规律（1xx/2xx/3xx/4xx） |
| 2 | `waynboot-common/.../support/order/submit/chain/OrderSubmitStep.java` | 责任链步骤接口，6 个常量就是下单的 6 个阶段 |
| 3 | `waynboot-common/.../support/order/submit/chain/OrderSubmitChain.java` | 责任链执行器，核心逻辑只有 10 行 |
| 4 | `waynboot-common/.../support/order/OrderStateTransitionSupport.java` | 订单状态机，构造函数里 4 行就是全部合法流转 |
| 5 | `waynboot-common/.../support/order/OrderStockSupport.java` | 库存四个动作：冻结 / 确认 / 释放 / 回补 |

---

## 二、模块职责一句话总结

```
waynboot-admin-api       ← 后台管理 HTTP 入口（端口 81），不写业务逻辑
waynboot-mobile-api      ← H5 商城 HTTP 入口（端口 82），不写业务逻辑
waynboot-common          ← 所有业务逻辑都在这里
waynboot-data-redis      ← Redis 工具封装，Lua 脚本在 RedisCache.java
waynboot-data-elastic    ← Elasticsearch 工具封装
waynboot-message-core    ← RabbitMQ 队列/交换机定义
waynboot-message-consumer← MQ 消费者（端口 85）
waynboot-monitor         ← Spring Boot Admin（端口 89）
waynboot-util            ← 枚举、异常、常量，不依赖任何其他模块
```

**规律**：Controller 只做参数校验和 VO 转换，真正的业务逻辑全部在 `waynboot-common` 的 `support/` 包下。

---

## 三、代码分层示意

```
HTTP 请求
  └─ Controller（waynboot-admin-api 或 waynboot-mobile-api）
       └─ Service 接口（waynboot-common/core/service/shop/）
            └─ Support 类（waynboot-common/core/service/shop/support/）
                 ├─ 读数据：Mapper（MyBatis-Plus）
                 ├─ 写缓存：RedisCache
                 └─ 发消息：LocalMessageService → RabbitMQ
```

Support 类是核心。每个 Support 类只负责一个切面，例如：
- `OrderValidationSupport` — 只做校验，不写库
- `OrderStockSupport` — 只操作库存，不管订单状态
- `OrderStateTransitionSupport` — 只管状态流转规则，不写库
- `OrderCancellationSupport` — 只做取消补偿，组合上面几个

---

## 四、追踪一次下单请求

**入口**：`waynboot-mobile-api` → `SubmitOrderController.submitOrder()`

**链路**：

```
SubmitOrderController.submitOrder()
  └─ IMobileOrderService.submitOrder()
       └─ OrderSubmitSupport.submit()
            └─ OrderSubmitChain.execute(context)
                 ├─ step 100: OrderSubmitDuplicateCheckStep   // 查重，重复则 context.stop()
                 ├─ step 200: OrderSubmitContextBuildStep     // 加载购物车、地址、优惠券
                 ├─ step 300: OrderSubmitStockReduceStep      // Redis Lua 预占 + MySQL 冻结库存
                 ├─ step 400: OrderSubmitEntityBuildStep      // 组装 Order 和 OrderGoods 对象
                 ├─ step 500: OrderSubmitSinglePersistStep    // 写订单主表和明细表
                 └─ step 600: OrderSubmitSingleDelayMessageStep // 写本地消息表，触发后置动作
```

**关键设计**：step 300 在 `finally` 块里释放 Redis 预占，无论后续步骤成功还是失败都不会泄漏。

---

## 五、追踪一次支付回调

**入口**：`waynboot-mobile-api` → `PayController`（微信/支付宝/易支付各一个方法）

**链路**：

```
PayController.wechatPayNotify() / alipayNotify() / epayNotify()
  └─ PaymentCallbackSupport.wxPayNotify() / aliPayNotify() / epayPayNotify()
       ├─ 验签（渠道差异只在这里）
       └─ markOrderPaid(orderSn, totalFee, payChannel, payId)  // 统一入口
            ├─ 校验订单金额
            ├─ TransactionTemplate 内：
            │    ├─ PaymentFlowService.savePaidFlow()  // 写支付流水（唯一键幂等）
            │    └─ 条件更新订单状态 101→201       // applyExpectedStatus 防并发覆盖
            └─ PaymentPostActionSupport.handleOrderPaid()  // 写本地消息，异步确认库存
```

**关键设计**：`PaymentFlowService.savePaidFlow()` 返回 `DUPLICATE_CONFLICT` 时抛 `IllegalStateException`，触发 `TransactionTemplate` 回滚，区分"已被其他线程处理"和"真实失败"。

---

## 六、订单状态流转图

```
101 未付款 ──支付──→ 201 已付款 ──发货──→ 301 已发货 ──收货──→ 401 用户收货
    │                   │                                    └──→ 402 系统收货
    ├──用户取消──→ 102 用户取消
    └──超时取消──→ 103 系统取消
                   │
                   └──申请退款──→ 202 申请退款 ──退款确认──→ 203 退款成功
```

代码位置：`OrderStateTransitionSupport` 构造函数，4 行 `transitions.put(...)` 就是上图的全部规则。

任何状态变更都必须调用 `applyExpectedStatus(wrapper, sourceStatus)`，在 SQL 的 WHERE 条件里带上当前状态，防止并发请求互相覆盖。

---

## 七、库存模型

每个 SKU（`shop_goods_product` 表）有两个字段：

| 字段 | 含义 |
|---|---|
| `number` | 可售库存（用户能买的数量） |
| `locked_stock` | 冻结库存（已下单未支付的数量） |

四个动作：

```
下单冻结：number - n，locked_stock + n   ← OrderStockSupport.freezeStock()
支付确认：locked_stock - n               ← OrderStockSupport.confirmFrozenStockByOrderId()
取消释放：number + n，locked_stock - n   ← OrderStockSupport.releaseFrozenStockByOrderId()
退款回补：number + n                     ← OrderStockSupport.restoreStock()
```

**幂等保证**：每次库存变更前先写 `shop_inventory_flow` 流水表（唯一键 = 操作类型 + 业务ID + 产品ID）。如果流水已存在，跳过库存更新。这样本地消息重试时不会重复扣减。

---

## 八、本地消息表（Outbox 模式）

**解决的问题**：业务数据写库成功，但 MQ 投递失败，导致后置动作（确认库存、更新虚拟销量）丢失。

**流程**：

```
业务事务内：写业务数据 + 写 local_message（同一个事务）
     ↓
LocalMessageRelaySupport 定时扫描 status=INIT 的消息
     ↓
投递 RabbitMQ 或调用本地 LocalMessageHandler
     ↓
成功 → status=SENT
失败 → 指数退避重试（1s, 2s, 4s...最大 60s），超过 5 次 → status=FAILED
     ↓
FAILED 消息可通过 /ops/trade/local-message/{id}/retry 人工重投
```

**幂等保证**：`local_message.message_key` 有唯一索引，重复插入被 `DuplicateKeyException` 静默吞掉。

---

## 九、支付策略模式

支付渠道通过策略模式扩展，新增渠道只需：

1. 在 `PayTypeEnum` 加一个枚举值
2. 实现 `PayTypeInterface`，加 `@Component`
3. `PayTypeContext` 会自动发现并注册

代码位置：`waynboot-common/design/strategy/pay/`

退款渠道同理：`waynboot-common/design/strategy/refund/`

---

## 十、测试规范

所有测试在 `waynboot-common/src/test/`，纯单元测试，不启动 Spring 容器，不连数据库。

**运行测试**：
```bash
mvn test -pl waynboot-common
```

**注意**：测试中使用 MyBatis-Plus Lambda Wrapper（如 `Wrappers.lambdaQuery(Order.class)`）时，需要先初始化 `TableInfo` 缓存，否则会报 `TableInfoHelper` 异常。参考 `MybatisPlusTableInfoTestHelper` 工具类的用法。

---

## 十一、常见问题

**Q：为什么 Controller 里几乎没有业务逻辑？**  
A：这是刻意设计。Controller 只做 HTTP 适配（参数校验、权限、日志、VO 转换），业务逻辑集中在 `waynboot-common` 便于单元测试和复用。

**Q：Support 类和 Service 类有什么区别？**  
A：Service 接口是对外暴露的能力边界（通常对应一张表或一个聚合根）。Support 类是内部实现的能力切片，不对外暴露接口，只被同包或上层 Service 调用。

**Q：为什么库存要用 Redis Lua 预占，而不是直接操作 MySQL？**  
A：高并发下单时，MySQL 行锁会成为瓶颈。Redis Lua 脚本原子执行，先在内存里做并发门控，只有预占成功的请求才进入 MySQL，大幅减少数据库压力。MySQL 的条件更新是最终一致性保障，防止 Redis 和 MySQL 不一致时超卖。

**Q：`applyExpectedStatus` 是什么？**  
A：它在 SQL UPDATE 的 WHERE 条件里加上 `order_status = 当前状态`。如果两个并发请求同时尝试修改同一订单，只有一个能更新成功（`updated == 1`），另一个得到 `updated == 0` 后抛异常，避免状态被覆盖。

**Q：本地消息表和 MQ 有什么关系？**  
A：本地消息表是 MQ 的可靠性补偿层。MQ 投递成功后消息状态变为 SENT，不再重试。如果 MQ 宕机，消息停留在 INIT，等 MQ 恢复后 Relay 会自动补投。

**Q：定时任务写在哪里？**  
A：项目用 Spring `@Scheduled`，没有独立的 job 模块。

- 业务侧定时入口都在 `*Application` 同包或下一级，搜 `@Scheduled` 注解即可。
- 本地消息 relay：`waynboot-common/.../message/LocalMessageRelayTask`，每 5 秒一次。
- 交易治理（库存快照、库存对账、支付日终对账）：`waynboot-admin-api/.../schedule/TradeGovernanceScheduledTask`，cron 表达式与批量参数集中在 `TradeScheduleProperties`，可通过 `wayn.schedule.trade.*` 覆盖默认值。
- 调度异常由 Spring 默认 `TaskUtils.LoggingErrorHandler` 记录后继续下次调度，任务方法本身不需要写 try/catch。

---

## 十二、推荐阅读顺序（按复杂度递增）

1. `OrderStatusEnum` — 理解状态码
2. `OrderStateTransitionSupport` — 理解状态机
3. `OrderSubmitStep` + `OrderSubmitChain` — 理解责任链
4. `OrderStockSupport` — 理解库存模型
5. `PaymentCallbackSupport` — 理解幂等回调
6. `LocalMessageService` + `LocalMessageRelaySupport` — 理解 Outbox 模式
7. `RedisStockPreDeductSupport` — 理解 Redis 预占
8. `OrderSubmitStockReduceStep` — 把 Redis 预占和 MySQL 冻结串起来看
