# 新手上手指南

本文档面向第一次接触 waynboot-mall 的开发者，帮助你在 30 分钟内建立对代码结构的整体认知，并能独立追踪一条完整的业务链路。

---

## 一、先看这几个文件

按顺序阅读，每个文件都不超过 120 行：

| 顺序 | 文件 | 读完你会明白 |
|---|---|---|
| 1 | `waynboot-util/.../enums/OrderStatusEnum.java` | 订单状态码的含义和分组规律（1xx/2xx/3xx/4xx） |
| 2 | `waynboot-domain-trade/.../support/order/submit/chain/OrderSubmitStep.java` | 责任链步骤接口，6 个常量就是下单的 6 个阶段 |
| 3 | `waynboot-domain-trade/.../support/order/submit/chain/OrderSubmitChain.java` | 责任链执行器，核心逻辑只有 10 行 |
| 4 | `waynboot-domain-trade/.../support/order/OrderStateTransitionSupport.java` | 订单状态机，构造函数里 4 行就是全部合法流转 |
| 5 | `waynboot-domain-inventory/.../support/OrderStockSupport.java` | 库存四个动作：冻结 / 确认 / 释放 / 回补 |

---

## 二、模块职责一句话总结

```
waynboot-admin-api       ← 后台管理 HTTP 入口（端口 81），不写业务逻辑
waynboot-mobile-api      ← H5 商城 HTTP 入口（端口 82），不写业务逻辑
waynboot-domain-api      ← 跨领域契约：entity / Mapper / Service 接口 / VO / 枚举
waynboot-domain-trade    ← 订单、支付回调、状态机、本地消息、对账
waynboot-domain-inventory← 库存冻结、库存流水、Redis 库存快照、库存对账
waynboot-domain-goods    ← 商品、SKU、类目、搜索、ES 同步
waynboot-domain-cart     ← 购物车读写和选中商品聚合
waynboot-domain-promotion← 优惠券、营销位和 Diamond 策略实现
waynboot-payment-channel ← 微信、支付宝、易支付的支付 / 退款渠道适配
waynboot-common          ← 通用配置、切面、策略接口、通用模型和基础设施
waynboot-data-redis      ← Redis 工具封装，Lua 脚本在 RedisCache.java
waynboot-data-elastic    ← Elasticsearch 工具封装
waynboot-message-core    ← RabbitMQ 队列/交换机定义
waynboot-message-consumer← MQ 消费者（端口 85）
waynboot-util            ← 枚举、异常、常量，不依赖任何其他模块
```

**规律**：Controller 只做参数校验和 VO 转换，真正的业务逻辑在各 `waynboot-domain-*` 模块的 `service/`、`support/`、`outbox/` 包下。

---

## 三、代码分层示意

```
HTTP 请求
  └─ Controller（waynboot-admin-api 或 waynboot-mobile-api）
       └─ Service 接口（waynboot-domain-api/.../service/）
            └─ ServiceImpl / Support 类（waynboot-domain-*）
                 ├─ 读数据：Mapper（MyBatis-Plus）
                 ├─ 写缓存：RedisCache
                 └─ 发消息：LocalMessageService → RabbitMQ
```

Support 类是核心。每个 Support 类只负责一个切面，例如：
- `OrderValidationSupport`（trade）— 只做校验，不写库
- `OrderStockSupport`（inventory）— 只操作库存，不管订单状态
- `OrderStateTransitionSupport`（trade）— 只管状态流转规则，不写库
- `OrderCancellationSupport`（trade）— 只做取消补偿，组合订单状态和库存能力

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
                       ┌──发货──→ 301 已发货 ──收货──→ 401 用户收货
                       │                            └──→ 402 系统收货
101 未付款 ──支付──→ 201 已付款
    │                  │
    │                  └──申请退款──→ 202 申请退款 ──退款确认──→ 203 退款成功
    │
    ├──用户取消──→ 102 用户取消（终态）
    └──超时取消──→ 103 系统取消（终态）
```

代码位置：`OrderStateTransitionSupport` 构造函数，4 个 `transitions.put(...)` 就是上图的全部规则。

**终态**：102 / 103 / 203 / 401 / 402 都不再有出边。退款链路只能从 201 已付款进入，未付款被取消的订单（102/103）没有钱可退。

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

### 7.1 两层闸门 + 一份账本

库存写入链路有三个角色，理解它们各自的职责能让你知道哪里在防多卖、哪里在做最终一致：

| 层级 | 角色 | 数据源 | 关键代码 |
|---|---|---|---|
| L1 入口闸门 | Redis 预扣（30s TTL） | `trade:stock:available:{productId}` Lua CAS | `RedisStockPreDeductSupport.preDeduct`、`RedisCache.luaReserveStock` |
| L2 持久层 | MySQL 条件更新 | `shop_goods_product.number / locked_stock` | `GoodsProductMapper.xml` 的 freezeStock / confirmFrozenStock / releaseFrozenStock |
| 账本 | 库存流水 | `shop_inventory_flow`，flowKey 唯一键幂等 | `InventoryFlowServiceImpl.saveFlow` |

**关键事实**：

- `shop_goods_product.number` 已经是"可售量"（不是 total）。表里**没有** `total_stock` 字段，下单冻结直接 `number -= n`。
- 支付确认时**只动 `locked_stock`**（`locked_stock -= n`），不动 `number`，因为下单时 `number` 已减。这是初学者最容易看错的地方。
- Redis 仅承担"入口削峰"。它崩了、漂移了、TTL 过期了，**都不会引发多卖**——下游的 MySQL 条件更新还会再卡一道。

### 7.2 防多卖 / 防少卖的真正机制

**防多卖**（卖出超过实际库存）由两道关卡保证：

1. **MySQL 条件更新** `where number >= #{number}`（`GoodsProductMapper.xml:40-46`）：超卖时更新 0 行，`OrderSubmitStockReduceStep` 抛 `BusinessException`，订单失败。**这才是真正的护栏**。
2. **Redis Lua CAS**（`RedisCache.luaReserveStock`）：返回 -1 时业务侧立即拒单，避免大批请求穿透到 MySQL。这是性能闸门，不是正确性护栏。

**防少卖**（有货却被拒）：MySQL 永远是真值，下单链路最终读 MySQL，不会真的少卖；但 Redis 快照若漂移到比 MySQL 低，会让一部分请求"提前被 Redis 拒掉"造成用户感知层面的少卖——靠下面的定时任务 5 分钟内自愈。

### 7.3 库存一致性兜底（两个定时任务的能力边界）

`TradeGovernanceScheduledTask` 里有两个库存相关的任务，别把它们当成防多卖的防线，它们是事后修复：

| 任务 | 周期 | 修得了 | 修不了 |
|---|---|---|---|
| `refreshStockSnapshot` | 5 分钟 | Redis 快照漂移（少写、误过期、Redis 重启） | MySQL 自身漂移；修复前的窗口内 Redis 误判仍然可能发生（但 MySQL 兜底，不会真多卖） |
| `reconcileInventory` | 1 小时 | `locked_stock` 多记/少记（取消事务部分提交、补偿丢失），`repair=true` 时自动改 `locked_stock` | `number` 自身漂移（账本只能推 `locked_stock`）；订单状态 ↔ 库存的跨表一致性 |

**一句话总结**：
- **多卖**：完全防住，MySQL 条件更新挡死，定时任务在不在都不影响。
- **少卖**：理论上几乎不会真发生（MySQL 仍是真值）；Redis 快照漂移导致的"用户感知少卖"由 `refreshStockSnapshot` 5 分钟内自愈。
- **`locked_stock` 漂移**：靠 `reconcileInventory` 每小时审计 + 可选自动修复。
- **`number` 漂移 / 跨表不一致**：当前没有自动兜底，需要人工介入（一般只有人工改库或硬件异常才会触发）。

### 7.4 失败场景速查

| 场景 | 系统行为 |
|---|---|
| Redis 预扣成功 + MySQL freezeStock 失败 | `OrderSubmitStockReduceStep` 的 finally 无条件释放 Redis 预占，订单失败 |
| MySQL 已扣 + 应用崩溃 | freezeStock 是 `@Transactional`，流水写入和库存更新同事务，崩溃整体回滚 |
| 支付回调消息消费两次 | flowKey 唯一键命中 `DuplicateKeyException`，第二次直接跳过库存更新 |
| 取消订单时 Redis 预占已过期（30s TTL） | 不需要操作 Redis，仅 MySQL 释放 `locked_stock` |

---

## 八、本地消息表（Outbox 模式）

**解决的问题**：业务数据写库成功，但 MQ 投递失败，导致后置动作（确认库存、更新虚拟销量）丢失。

**为什么不在下单时直接发 MQ？**

MQ 投递和数据库写入不在同一个 ACID 事务里，直接发会有两种必然失败的场景：

```
先写库再发 MQ：订单写库成功，MQ 发失败 → 库存确认永远不执行
先发 MQ再写库：MQ 发成功，订单写库失败 → 消费者处理一个不存在的订单
```

Outbox 模式把"发消息"变成"写一行数据库记录"，让它和业务数据进同一个事务，把原子性问题从"数据库 + MQ"降级为"只需要数据库"。

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

### 8.1 两张表的职责

`local_message.sql` 里有两张表，分别解决“消息当前状态”和“补偿历史追踪”两个问题：

| 表 | 作用 | 典型使用场景 |
|---|---|---|
| `local_message` | 保存本地消息当前状态，是 Outbox 模式的主表 | 业务事务内写入消息，Relay 扫描 `INIT` 消息并投递 MQ 或执行本地 Handler |
| `local_message_compensation_log` | 保存失败、死信和人工重投的历史流水 | 排查消息为什么失败、失败过几次、是否进入死信、谁触发过人工重投 |

可以把两张表理解成：

```
local_message
  负责回答：这条消息现在处理到哪了？

local_message_compensation_log
  负责回答：这条消息失败和补偿的历史过程是什么？
```

`local_message` 是最终一致性的核心表。订单、支付、库存等业务数据写库时，同一个事务里写入一条本地消息。只要事务提交成功，后续异步副作用就不会因为 MQ 短暂不可用而丢失。

关键字段：

| 字段 | 含义 |
|---|---|
| `message_key` | 业务唯一消息键，依赖唯一索引实现幂等写入 |
| `topic` | 消息主题，例如 `ORDER_SUBMIT`、`ORDER_UNPAID_DELAY`、`ORDER_PAID_POST_ACTION` |
| `biz_type` / `biz_id` | 业务定位字段，方便按订单号或业务类型排查 |
| `exchange_name` / `routing_key` | RabbitMQ 路由信息；为空时表示走本地 `LocalMessageHandler` |
| `payload` | JSON 消息体 |
| `status` | 当前状态：`0` = `INIT`，`1` = `SENT`，`2` = `FAILED` |
| `retry_count` | 已重试次数 |
| `next_retry_time` | 下一次允许 Relay 重试的时间 |
| `last_error` | 最近一次失败原因，只保留最新错误 |

`local_message_compensation_log` 不参与主业务事务，也不决定消息是否继续投递。它是补偿审计表，用来保留每一次失败和人工操作记录，避免只看 `local_message.last_error` 时丢失历史。

关键字段：

| 字段 | 含义 |
|---|---|
| `message_id` / `message_key` | 对应 `local_message` 的消息 |
| `action_type` | 补偿动作：`FAILURE`、`DEAD_LETTER`、`MANUAL_RETRY` |
| `failure_reason` | 失败分类，例如 RabbitMQ 不可用、Redis 不可用、Handler 不存在或未知异常 |
| `retry_count` | 记录该次失败或补偿发生时的重试次数 |
| `dead_letter` | 是否已经超过最大重试次数并进入死信 |
| `operator` | 操作者；自动重试为 `system`，人工重投记录真实操作人 |
| `remark` | 错误摘要或人工操作备注 |

`last_error` 和补偿日志的区别：

```
local_message.last_error
  只看最近一次失败，适合快速判断当前卡在哪里。

local_message_compensation_log
  查看完整失败轨迹，适合定位是否反复失败、是否进入死信、是否有人处理过。
```

例如一条支付成功后置动作消息连续失败 5 次，`local_message.last_error` 最终只保留第 5 次错误；`local_message_compensation_log` 会保留前 4 次 `FAILURE` 和最后一次 `DEAD_LETTER`，后续人工重投还会追加 `MANUAL_RETRY` 记录。

---

## 九、后台首页统计逻辑

后台首页统计用于支撑管理端 `waynboot-admin/src/views/dashboard` 页面。历史问题是前端维护 mock 商品、固定库存预警和静态趋势数据，页面看起来“有数据”，但无法反映真实订单、商品和库存状态。现在统计统一收敛到后端，前端只做接口调用、字段归一化和图表展示。

### 9.1 代码入口

后端入口：

```text
waynboot-admin-api
└─ com.wayn.admin.api.controller.shop.DashboardController
   └─ DashboardService
      ├─ IOrderService / AdminOrderMapper
      ├─ IMemberService / MemberMapper
      ├─ IGoodsService
      └─ IGoodsProductService
```

前端入口：

```text
waynboot-admin
└─ src/views/dashboard/index.vue
   ├─ src/api/shop/dashboard.js
   └─ src/views/dashboard/dashboardData.js
```

请求链路：

```text
Dashboard 页面
  └─ getDashboard* API
       └─ GET /shop/dashboard/*
            └─ DashboardController
                 └─ DashboardService
                      ├─ MyBatis-Plus Wrapper 聚合
                      ├─ AdminOrderMapper 自定义 SQL
                      └─ Dashboard*VO 返回前端
```

`DashboardController` 只负责权限、日志和响应封装。所有接口复用 `@ss.hasPermi('shop:dashboard:stats')` 权限点，统计规则不要写在 Controller 中。

### 9.2 接口清单

| 接口 | 返回 VO | 用途 |
|---|---|---|
| `GET /shop/dashboard/stats` | `DashboardStatsVO` | 首页顶部核心指标和订单状态卡片 |
| `GET /shop/dashboard/trend` | `DashboardTrendVO` | 近 7 日销售趋势折线图 |
| `GET /shop/dashboard/period` | `DashboardPeriodVO` | 今日、本周、本月周期统计和环比 |
| `GET /shop/dashboard/payment-channel` | `List<DashboardChannelVO>` | 支付渠道订单占比 |
| `GET /shop/dashboard/top-goods` | `List<DashboardTopGoodsVO>` | 热销商品榜 |
| `GET /shop/dashboard/low-stock-goods` | `List<DashboardTopGoodsVO>` | 库存预警榜 |
| `GET /shop/dashboard/member-trend` | `DashboardMemberTrendVO` | 近 30 日会员新增趋势 |
| `GET /shop/dashboard/recent` | `DashboardRecentVO` | 最近订单和最近会员 |

### 9.3 订单统计口径

首页经营数据不是简单统计全部订单。`DashboardService` 里有一组已支付生命周期状态：

```text
STATUS_PAY           已付款
STATUS_REFUND        申请退款
STATUS_SHIP          已发货
STATUS_CONFIRM       用户确认收货
STATUS_AUTO_CONFIRM  系统自动确认收货
```

这些状态代表订单已经产生支付事实或进入履约链路，因此用于销售额、支付渠道和支付转化率统计。未付款、用户取消、系统取消不计入销售额，避免把未成交订单算成经营收入。

核心指标规则：

| 指标 | 计算方式 |
|---|---|
| 今日订单 | `shop_order.create_time >= 今日 00:00` 的订单数 |
| 今日销售额 | 今日已支付生命周期订单的 `actual_price` 求和 |
| 累计销售额 | 全部已支付生命周期订单的 `actual_price` 求和 |
| 支付转化率 | 今日已支付生命周期订单数 / 今日订单数 |
| 待付款 | `order_status = STATUS_CREATE` |
| 待发货 | `order_status = STATUS_PAY` |
| 待收货 | `order_status = STATUS_SHIP` |
| 已完成 | `STATUS_CONFIRM` + `STATUS_AUTO_CONFIRM` |
| 已关闭 | `STATUS_CANCEL` + `STATUS_AUTO_CANCEL` |
| 退款申请 | `order_status = STATUS_REFUND` |

注意：当前退款成功订单没有计入销售额口径。如果后续要统计净销售额，需要引入退款金额抵扣规则，而不是简单把退款成功订单加回销售额。

### 9.4 趋势和周期统计

`trend()` 查询近 7 日数据，SQL 按 `DATE(create_time)` 分组，返回：

```text
dates        横轴日期，格式 MM-dd
orderCounts 订单数
sales       已支付生命周期订单销售额
```

服务层会从今天往前补齐 7 天。如果某一天没有订单，返回 0，而不是缺少这个日期。这样前端 ECharts 横轴稳定，不会因为某天无数据导致点位错位。

`period()` 统计今日、本周、本月，并和上一个同周期做环比：

| 周期 | 当前区间 | 对比区间 |
|---|---|---|
| 今日 | 今日 00:00 到当前时间 | 昨日 00:00 到今日 00:00 |
| 本周 | 本周一 00:00 到当前时间 | 上周一 00:00 到本周一 00:00 |
| 本月 | 本月 1 日 00:00 到当前时间 | 上月 1 日 00:00 到本月 1 日 00:00 |

时间区间使用左闭右开规则：`create_time >= start` 且 `create_time < end`。这样边界时间只会落入一个周期，不会重复统计。

### 9.5 支付渠道统计

`paymentChannel()` 按 `shop_order.pay_type` 分组，统计已支付生命周期订单的订单数和销售额。

```sql
select pay_type,
       count(*) as cnt,
       sum(actual_price) as sales
from shop_order
where order_status in (...)
group by pay_type;
```

渠道名通过 `PayTypeEnum` 转换。当前代码额外兼容 `WX_JSAPI` 枚举描述历史不准确的问题，看板侧直接展示“微信 JSAPI”。无法识别的渠道统一归入“其他”。

### 9.6 热销商品统计

热销商品必须来自真实订单明细，不再使用商品表的展示销量字段。

真实数据来源：

```text
shop_order_goods.number  订单明细购买数量
shop_order.order_status  订单支付生命周期状态
```

核心 SQL 位于 `AdminOrderMapper.selectTopGoodsByPaidOrders`：

```sql
select g.goods_id as goodsId,
       sum(g.number) as actualSales
from shop_order_goods g
join shop_order o on o.id = g.order_id
where g.del_flag = 0
  and o.del_flag = 0
  and o.order_status in (...)
group by g.goods_id
order by actualSales desc, g.goods_id asc
limit 5;
```

为什么不用 `shop_goods.actual_sales`：

- `actual_sales` 更偏商品维护或展示字段，可能被运营导入、后台编辑或脚本修正。
- 首页热销榜需要回答“最近真实订单卖了什么”，应该以订单明细为准。
- 如果继续用 `actual_sales`，前端看起来就像写死数据，订单变化后榜单不一定变化。

`DashboardService.topGoods()` 的组装流程：

```text
1. AdminOrderMapper 聚合已支付订单明细销量
2. 用 LinkedHashMap 保留 SQL 返回的销量排序
3. 批量查询 shop_goods 获取商品名、图片、价格、SKU 编码
4. 批量查询 shop_goods_product，按商品取最小 SKU 可售库存
5. 组装 DashboardTopGoodsVO 返回前端
```

这里使用 `LinkedHashMap` 是为了保留数据库已经算好的热销排名。普通 `HashMap` 不保证顺序，可能导致前端榜单顺序和 SQL 排序不一致。

### 9.7 库存预警统计

库存预警和热销商品是两套独立数据，不能复用热销榜。

数据来源：

```text
shop_goods_product.number
```

规则：

```text
number <= 10       进入低库存候选
按 number 升序      库存越低越靠前
按 goodsId 聚合     同一个商品多个 SKU 时取最小库存
最多返回 5 个商品
```

服务层先按 SKU 取 `TOP_GOODS_LIMIT * 3` 条低库存货品，再聚合到商品维度。这样可以避免同一个商品多个低库存 SKU 把列表占满，同时尽量保证最终能返回 5 个不同商品。

### 9.8 会员和最近动态

`memberTrend()` 查询近 30 日新增会员，按 `DATE(create_time)` 分组，并补齐缺失日期。它和订单趋势的处理方式一致，都是为了保证前端图表横轴稳定。

`recent()` 返回对象结构：

```json
{
  "orders": [],
  "members": []
}
```

订单按 `shop_order.create_time desc` 取最近 5 条，会员按 `shop_member.create_time desc` 取最近 5 条。前端不要再把最近订单和最近会员混成同一个数组处理。

### 9.9 前端适配规则

前端入口集中在 `waynboot-admin/src/views/dashboard`：

| 文件 | 作用 |
|---|---|
| `src/api/shop/dashboard.js` | 封装 `/shop/dashboard/*` 请求 |
| `src/views/dashboard/index.vue` | 页面编排、图表渲染、卡片展示 |
| `src/views/dashboard/dashboardData.js` | 后端 VO 到前端展示模型的字段归一化 |

前端字段归一化规则：

- 商品销量优先读取 `actualSales`，兼容 `sales`、`saleCount`。
- 商品价格优先读取 `retailPrice`，兼容 `price`、`salesPrice`。
- 库存优先读取 `stock`，兼容 `goodsStock`、`inventory`、`stockNum`。
- 最近动态如果后端返回数组，兼容为 `{ orders: data, members: [] }`；新接口应返回对象结构。

排查首页“还是写死数据”时，按这个顺序看：

```text
1. 浏览器 Network 查看 /shop/dashboard/top-goods 返回值
2. 如果接口返回真实数据但页面不变，检查 dashboardData.js 字段归一化
3. 如果接口返回旧数据，检查 DashboardService.topGoods()
4. 如果 topGoods() 结果不变，检查 shop_order_goods 是否有已支付订单明细
5. 如果订单明细有数据但榜单为空，检查订单状态是否在 PAID_LIFECYCLE_STATUSES 内
```

### 9.10 推荐索引

首页统计多数是聚合查询。数据量上来后，需要重点关注这些索引：

```sql
-- 订单趋势、状态卡片、周期统计
KEY idx_status_create_time(order_status, create_time);

-- 用户订单列表和最近订单
KEY idx_user_create_time(user_id, create_time);

-- 热销商品聚合
KEY idx_order_goods_order_id(order_id);
KEY idx_order_goods_goods_id(goods_id);

-- 库存预警
KEY idx_goods_product_number(number);

-- 会员趋势
KEY idx_member_create_time(create_time);
```

当前 README 只保留首页统计概览，完整维护说明以本章节为准。

---

## 十、支付策略模式

支付渠道通过策略模式扩展，新增渠道只需：

1. 在 `PayTypeEnum` 加一个枚举值
2. 实现 `PayTypeInterface`，加 `@Component`
3. `PayTypeContext` 会自动发现并注册

策略接口位置：`waynboot-common/src/main/java/com/wayn/common/design/strategy/pay/`

渠道实现位置：`waynboot-payment-channel/src/main/java/com/wayn/payment/channel/pay/`

退款策略接口同理：`waynboot-common/src/main/java/com/wayn/common/design/strategy/refund/`

退款渠道实现位置：`waynboot-payment-channel/src/main/java/com/wayn/payment/channel/refund/`

---

## 十一、测试规范

测试已跟随模块拆分分布在各模块的 `src/test/java` 下，优先看 `waynboot-domain-trade`、`waynboot-domain-inventory`、`waynboot-domain-goods` 和入口模块测试。

**运行测试**：
```bash
mvn test
mvn test -pl waynboot-domain-trade
mvn test -pl waynboot-domain-inventory
```

**注意**：测试中使用 MyBatis-Plus Lambda Wrapper（如 `Wrappers.lambdaQuery(Order.class)`）时，需要先初始化 `TableInfo` 缓存，否则会报 `TableInfoHelper` 异常。参考 `MybatisPlusTableInfoTestHelper` 工具类的用法。

---

## 十二、常见问题

**Q：为什么 Controller 里几乎没有业务逻辑？**  
A：这是刻意设计。Controller 只做 HTTP 适配（参数校验、权限、日志、VO 转换），业务逻辑集中在各 `waynboot-domain-*` 模块，便于单元测试和复用。

**Q：Support 类和 Service 类有什么区别？**  
A：Service 接口是对外暴露的能力边界（通常对应一张表或一个聚合根）。Support 类是内部实现的能力切片，不对外暴露接口，只被同包或上层 Service 调用。

**Q：为什么库存要用 Redis Lua 预占，而不是直接操作 MySQL？**  
A：高并发下单时，MySQL 行锁会成为瓶颈。Redis Lua 脚本原子执行，先在内存里做并发门控，只有预占成功的请求才进入 MySQL，大幅减少数据库压力。MySQL 的条件更新是最终一致性保障，防止 Redis 和 MySQL 不一致时超卖。展开看「七、7.2 防多卖 / 防少卖的真正机制」。

**Q：`applyExpectedStatus` 是什么？**  
A：它在 SQL UPDATE 的 WHERE 条件里加上 `order_status = 当前状态`。如果两个并发请求同时尝试修改同一订单，只有一个能更新成功（`updated == 1`），另一个得到 `updated == 0` 后抛异常，避免状态被覆盖。

**Q：本地消息表和 MQ 有什么关系？**  
A：本地消息表是 MQ 的可靠性补偿层。MQ 投递成功后消息状态变为 SENT，不再重试。如果 MQ 宕机，消息停留在 INIT，等 MQ 恢复后 Relay 会自动补投。

**Q：定时任务写在哪里？**  
A：项目用 Spring `@Scheduled`，没有独立的 job 模块。

- 业务侧定时入口都在 `*Application` 同包或下一级，搜 `@Scheduled` 注解即可。
- 本地消息 relay：`waynboot-domain-trade/.../outbox/LocalMessageRelayTask`，每 5 秒一次。
- 交易治理（库存快照、库存对账、支付日终对账）：`waynboot-admin-api/.../schedule/TradeGovernanceScheduledTask`，cron 表达式与批量参数集中在 `TradeScheduleProperties`，可通过 `wayn.schedule.trade.*` 覆盖默认值。
- 调度异常由 Spring 默认 `TaskUtils.LoggingErrorHandler` 记录后继续下次调度，任务方法本身不需要写 try/catch。

---

## 十三、推荐阅读顺序（按复杂度递增）

1. `OrderStatusEnum` — 理解状态码
2. `OrderStateTransitionSupport` — 理解状态机
3. `OrderSubmitStep` + `OrderSubmitChain` — 理解责任链
4. `OrderStockSupport` — 理解库存模型
5. `PaymentCallbackSupport` — 理解幂等回调
6. `LocalMessageService` + `LocalMessageRelaySupport` — 理解 Outbox 模式
7. `RedisStockPreDeductSupport` — 理解 Redis 预占
8. `OrderSubmitStockReduceStep` — 把 Redis 预占和 MySQL 冻结串起来看
9. `DashboardService` + `AdminOrderMapper` — 理解后台首页统计口径
