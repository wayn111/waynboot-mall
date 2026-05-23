# waynboot-mall

`waynboot-mall` 是一个基于 Spring Boot 3、Java 17 的 Maven 多模块商城后端项目，覆盖移动端商城、后台管理、消息消费、定时任务、Redis、Elasticsearch 和监控等能力。项目当前重点围绕商品、订单、库存、支付、MQ、本地消息补偿和对账治理进行拆分，目标是让交易链路在高并发场景下更易维护、更可扩展。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 基础框架 | Spring Boot 3.1.4、Java 17 |
| Web 与安全 | Spring MVC、Spring Security、JWT |
| 数据访问 | MyBatis-Plus、MySQL、Druid |
| 缓存 | Redis、Lettuce、Lua 脚本 |
| 搜索 | Elasticsearch 7 |
| 消息 | RabbitMQ、延迟消息、本地消息表 |
| 定时任务 | Spring Scheduled |
| 监控 | Spring Boot Admin、Actuator、Micrometer、Prometheus |
| 支付 | 微信支付、支付宝、易支付 |
| 测试 | JUnit 5、Mockito、Spring Boot Test |

## 模块结构

```text
waynboot-mall
├── waynboot-admin-api              后台管理接口
├── waynboot-mobile-api             H5 / 移动端接口
├── waynboot-common                 核心业务、实体、Mapper、Service、VO、领域支撑类
├── waynboot-data
│   ├── waynboot-data-redis         Redis 访问与缓存工具
│   └── waynboot-data-elastic       Elasticsearch 访问能力
├── waynboot-message
│   ├── waynboot-message-core       RabbitMQ 队列、交换机、绑定配置
│   └── waynboot-message-consumer   MQ 消费者
├── waynboot-monitor                监控模块
├── waynboot-util                   通用工具、异常、枚举、常量
├── db-init                         数据库初始化和增量治理 SQL
├── redis / rabbitmq / es / nginx   中间件配置
└── docker-compose*.yml             本地或服务器编排脚本
```

## 架构分层

项目采用入口层、编排层、领域能力层、异步一致性层和治理层分层设计。

```text
Controller 入口层
  -> 参数校验、权限判断、日志记录、VO 返回封装

Service / Support 编排层
  -> 下单责任链、支付回调、订单状态流转、补偿入口

领域能力层
  -> 商品、购物车、订单、库存、优惠券、支付流水、订单状态日志

异步一致性层
  -> 本地消息表、MQ relay、消费者模板、补偿重试

治理层
  -> Redis 削峰、库存对账、支付对账、补偿后台、分表路由、Spring Scheduled 定时任务
```

入口模块不直接承载核心业务逻辑。`admin-api` 和 `mobile-api` 主要负责接口适配，交易逻辑集中在 `waynboot-common` 的 Service、support、assembler 和 helper 中。

## 核心业务能力

### 商品与购物车

- 商品后台维护商品、SKU、规格、属性、类目、库存等基础数据。
- 移动端商品详情支持聚合查询和 Redis 缓存。
- 商品写入、库存变更、虚拟销量变更后主动删除商品详情缓存，避免详情页展示旧库存。
- 购物车聚合商品、SKU、优惠券和价格信息，接口返回使用明确 VO，不直接暴露数据库实体。

### 订单链路

下单链路按责任链和领域支撑服务拆分，核心步骤包括：

```text
用户下单
-> 参数与购物车校验
-> Redis Lua 库存预占
-> MySQL 条件冻结库存
-> 写订单主表和订单明细
-> 写本地消息表
-> MQ / 本地处理器异步执行后置动作
```

订单状态统一由 `OrderStateTransitionSupport` 管理，支付、取消、发货、退款、确认收货等入口都必须校验状态流转，并通过数据库当前状态条件更新避免并发覆盖。

### 库存一致性

库存模型采用可售库存和冻结库存两态为主：

```text
number       可售库存
lockedStock  冻结库存
```

库存变化规则：

```text
下单冻结：number - n，lockedStock + n
支付确认：lockedStock - n
超时取消：number + n，lockedStock - n
退款回补：number + n
```

设计原则：

- Redis Lua 只做入口削峰和热点 SKU 并发闸门。
- MySQL 条件更新负责最终一致性，避免超卖。
- 库存流水通过唯一 `flowKey` 保证本地消息重试下的库存副作用幂等。
- `InventoryReconciliationService` 可根据库存流水推导冻结库存，识别并修复 `lockedStock` 差异。
- 热点 SKU 可通过 Redis 库存快照和分桶 Key 降低单 Key 竞争。

### 支付与对账

支付回调支持微信、支付宝和易支付。回调处理原则：

- 验签通过后才能进入业务处理。
- 订单状态更新必须带当前状态条件。
- 第三方支付流水通过 `shop_payment_flow.flow_key` 做唯一约束。
- 重复回调按幂等成功返回。
- 支付成功后的库存确认、虚拟销量等后置动作通过本地消息异步处理。

支付对账能力包括：

- 支付流水 vs 订单状态 / 订单金额。
- 渠道账单 vs 内部支付流水。
- 退款流水 vs 订单退款金额。
- Spring Scheduled 提供日终对账入口。

### 本地消息与补偿

本地消息表用于解决“业务事务提交成功，但 MQ 投递失败”问题。

```text
业务事务内写业务数据 + local_message
-> Relay 扫描 INIT 消息
-> 投递 RabbitMQ 或执行本地处理器
-> 成功标记 SENT
-> 失败指数退避重试
-> 超过最大次数进入 FAILED
-> 后台人工重投或定时补偿
```

补偿治理能力：

- `LocalMessageService` 负责消息创建、查询、成功标记、失败重试。
- `LocalMessageFailureClassifier` 负责失败原因分类。
- `LocalMessageCompensationLogService` 记录失败、死信和人工重投日志。
- `TradeOpsController` 提供失败消息查询、指标查看和人工重投接口。

### 分表与查询治理

项目已沉淀交易表按月分片规则，后续可接入 ShardingSphere 或动态表名路由。

```text
shop_order_yyyyMM
shop_order_goods_yyyyMM
shop_payment_flow_yyyyMM
local_message_yyyyMM
```

推荐索引：

```sql
-- 订单主表
UNIQUE KEY uk_order_sn(order_sn);
KEY idx_user_create_time(user_id, create_time);
KEY idx_status_create_time(order_status, create_time);

-- 订单明细表
KEY idx_order_goods_order_id(order_id);
KEY idx_order_goods_product_id(product_id);

-- 支付流水表
UNIQUE KEY uk_payment_flow_key(flow_key);
KEY idx_payment_flow_order_sn(order_sn);
KEY idx_payment_flow_pay_id(pay_channel, pay_id);

-- 本地消息表
UNIQUE KEY uk_local_message_key(message_key);
KEY idx_local_message_status_retry(status, next_retry_time);
KEY idx_local_message_biz(biz_type, biz_id);
```

## 运维治理接口

后台交易治理接口位于 `waynboot-admin-api` 的 `ops/trade` 路径下：

```text
GET  /ops/trade/local-message/failed?limit=50
GET  /ops/trade/local-message/metric
POST /ops/trade/local-message/{messageId}/retry?operator=admin
POST /ops/trade/stock/snapshot/refresh
POST /ops/trade/inventory/reconcile
POST /ops/trade/payment/reconcile
```

这些接口主要用于最终一致性排查、库存对账、Redis 库存快照刷新和支付日终对账。

## 定时任务

定时任务采用 Spring `@Scheduled` 实现，运行在 `waynboot-admin-api` 进程内，与管理端 `TradeOpsController` 共用同一份依赖。任务实现位于 `waynboot-admin-api` 的 `com.wayn.admin.api.schedule.TradeGovernanceScheduledTask`：

```text
refreshStockSnapshot         刷新 Redis 库存快照，支持热点 SKU 分桶（默认每 5 分钟）
reconcileInventory           库存流水对账，可受控自动修复 lockedStock（默认每小时整点）
reconcilePaymentDaily        支付日终对账（默认每日 02:00）
```

调度周期、批量大小和是否开启自动修复通过 `application.yml` 中的 `wayn.schedule.trade.*` 节点调整：

```yaml
wayn:
  schedule:
    trade:
      stock-snapshot:
        cron: 0 */5 * * * *
        limit: 500
        bucket-count: 1
      inventory-reconcile:
        cron: 0 0 * * * *
        limit: 500
        repair: true
      payment-reconcile:
        cron: 0 0 2 * * *
```

## 数据库脚本

核心脚本位于 `db-init`：

```text
wayn_shop_*.sql                  基础商城库表和初始化数据
inventory_stock.sql              冻结库存字段和库存流水表
local_message.sql                本地消息表和补偿日志表
order_status_log.sql             订单状态日志表
payment_flow.sql                 支付流水、渠道账单、退款流水表
trade_sharding_governance.sql    分表命名和索引治理建议
```

SQL 文件保留为数据库结构演进入口，提交前需要结合目标环境执行顺序和幂等性确认。

## 本地开发

### 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8+
- Redis 6+
- RabbitMQ 3+
- Elasticsearch 7

### 构建与测试

```bash
mvn clean package
mvn test
```

如需使用本地 Maven 仓库目录：

```bash
mvn "-Dmaven.repo.local=.m2/repository" test
```

### 启动服务

```bash
mvn -pl waynboot-admin-api spring-boot:run
mvn -pl waynboot-mobile-api spring-boot:run
mvn -pl waynboot-message/waynboot-message-consumer spring-boot:run
mvn -pl waynboot-monitor spring-boot:run
```

也可以用 IDE 分别启动对应模块的 `*Application`。

### 中间件

仓库提供 Docker Compose 编排文件：

```bash
docker-compose up -d
```

如果需要同时启动 Elasticsearch、RabbitMQ、Redis 和 MySQL，可根据本地环境选择 `docker-compose-es7-rabbitmq-redis-mysql.yml`。

## 开发规范

- 所有源码、XML、Markdown 使用 UTF-8。
- Controller 只做参数校验、权限、日志和 VO 返回封装，不写核心业务逻辑。
- API 返回优先使用明确的 `VO` / `ResVO`，不要用裸 `Map<String, Object>` 承载复杂结构。
- 交易、库存、支付、补偿、幂等逻辑必须补齐类注释、方法注释和关键分支注释。
- 涉及 MyBatis-Plus Lambda Wrapper 的纯单元测试，需要初始化实体 `TableInfo` 缓存。
- 默认不提交 `application*.yml`、`application*.yaml`、`application*.properties` 等本地配置文件。

## 推荐阅读代码入口

```text
OrderStockSupport                 库存冻结、确认、释放、退款回补
RedisStockPreDeductSupport         Redis Lua 库存预占
RedisStockSnapshotSupport          Redis 库存快照预热和分桶
OrderStateTransitionSupport        订单状态机
PaymentCallbackSupport             支付回调统一处理
PaymentReconciliationService       支付、渠道账单、退款对账
LocalMessageService                本地消息状态流转
LocalMessageRelaySupport           本地消息投递和本地处理器分发
TradeOpsController                 后台交易治理接口
TradeGovernanceScheduledTask       交易治理定时任务（Spring Scheduled）
TradeTableShardRouter              交易表分片命名规则
```

## 后续演进方向

- 接入真实渠道账单文件导入，形成完整支付日终对账闭环。
- 增加对账差异表和差异处理状态机。
- 将 `TradeTableShardRouter` 接入 ShardingSphere 或动态表名拦截器。
- 增加库存桶表，进一步降低热点 SKU 的 MySQL 单行锁竞争。
- 补充 Prometheus 指标：本地消息失败数、死信数、MQ 堆积、支付差异数、库存差异数、Redis 快照缺失数。
- 对商品详情、下单、支付回调和本地消息 relay 做压测基线。

## 前端项目

- H5 商城前端：`waynboot-mobile`
- 管理后台前端：`waynboot-admin`

后端项目只提供 API 和治理能力，前端启动和部署请参考对应前端仓库。

## 许可

本项目遵循仓库中的 `LICENSE`。
