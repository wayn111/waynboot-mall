# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

waynboot-mall 是一个 Spring Boot 3.1 / JDK 17 的开源 H5 商城后端，包含管理端 API、移动端 API、消息消费服务三个可独立部署的应用。

## 常用命令

```bash
# 构建（跳过测试），jar 输出到 ./jars/
mvn clean package -DskipTests

# 运行所有测试（有意义的测试全在 waynboot-common）
mvn test -pl waynboot-common

# 运行单个测试类
mvn test -pl waynboot-common -Dtest=PaymentCallbackSupportTest

# 运行单个测试方法
mvn test -pl waynboot-common -Dtest=PaymentCallbackSupportTest#markOrderPaid_alreadyPaid_skipsUpdate
```

## 本地启动

**前置中间件**：MySQL 8.0、Redis 3.0+、RabbitMQ 3.0+（需 delayed-message 插件）、Elasticsearch 7.x（需 IK 分词器和拼音插件）

1. 在 `wayn_shop` 数据库中导入项目根目录的 `wayn_shop_*.sql`
2. 编辑各模块的 `application-dev.yml`，填写本地中间件连接信息
3. 分别启动三个 Spring Boot 主类：
   - `AdminApplication` → 端口 81
   - `MobileApplication` → 端口 82
   - `MessageApplication` → 端口 85

**Docker 部署**：
```bash
# 中间件
docker-compose -f docker-compose-es7-rabbitmq-redis-mysql.yml up -d
# 应用
docker-compose up -d
```

## 模块结构

| 模块 | 职责 |
|---|---|
| `waynboot-common` | 所有领域实体、Mapper、Service、业务逻辑（核心模块） |
| `waynboot-admin-api` | 管理端 REST 控制器 + Spring Security 配置 + Spring `@Scheduled` 治理任务 |
| `waynboot-mobile-api` | H5 移动端 REST 控制器 |
| `waynboot-message` | RabbitMQ 消息消费（core + consumer 子模块） |
| `waynboot-data` | Redis / Elasticsearch 基础设施封装 |
| `waynboot-util` | 常量、枚举、异常、工具类 |

## 核心架构

### 业务领域（均在 `waynboot-common`）

所有业务逻辑通过 **Support 类**组织，而非直接写在 Service 实现里：

```
service/shop/support/
  order/          # 订单提交、取消、生命周期、库存扣减链
  admin/order/    # 管理端发货、退款
  payment/        # 支付回调幂等处理
  goods/          # 商品 ES 同步、变更
  cart/           # 购物车读写
  coupon/         # 优惠券领取
message/          # 本地消息表（outbox 模式）中继与补偿
trade/            # 分表路由
```

### 关键设计模式

**订单提交链**：`OrderSubmitStockReduceStep` 等实现责任链，Redis 预扣库存作为并发门控，MySQL 为最终数据源，`finally` 块保证 Redis 释放。

**支付回调幂等**：`PaymentCallbackSupport` 使用条件更新（`applyExpectedStatus`）+ 二次查询区分"已被其他线程处理"与"真实失败"。`PaymentFlowSaveResult.DUPLICATE_CONFLICT` 通过抛出 `IllegalStateException` 触发 `TransactionTemplate` 回滚。

**本地消息表（outbox）**：`LocalMessageService` 以 `DuplicateKeyException` 实现幂等插入，指数退避重试（最多 5 次），超限后转 `FAILED` 死信。

**状态机条件更新**：所有订单状态变更统一使用 `applyExpectedStatus(updateWrapper, sourceStatus)` 做乐观并发控制，`updated == 0` 时抛 `BusinessException`。

**Redis Lua 脚本**：`RedisCache` 中的库存预扣（`luaReserveStock`）和释放（`luaReleaseReservedStock`）均为原子 Lua 脚本，返回值语义：`1`=成功/幂等，`-1`=库存不足，`-2`=未初始化，`-3`=参数非法。

### 数据访问

- ORM：MyBatis-Plus，逻辑删除字段 `del_flag`（1=已删除）
- Mapper XML：`classpath*:mapper/**/*Mapper.xml`
- Redis：通过 `RedisCache` 统一封装，避免直接使用 `RedisTemplate`

### 认证

- 管理端（端口 81）：JWT，有效期 120 分钟，header `Authorization`
- 移动端（端口 82）：JWT，有效期 30 天
- 框架：Spring Security，配置在各 API 模块的 `framework/` 包下

### 定时任务

Spring `@Scheduled` 调度，运行在 `AdminApplication` 进程内：

- 入口类：`waynboot-admin-api` → `com.wayn.admin.api.schedule.TradeGovernanceScheduledTask`
- 配置类：`com.wayn.admin.api.schedule.TradeScheduleProperties`，前缀 `wayn.schedule.trade.*`
- 业务侧 relay：`LocalMessageRelayTask`（`waynboot-common`，每 5 秒一次）
- 失败处理由 Spring 默认 `TaskUtils.LoggingErrorHandler` 兜底，任务方法不需要 try/catch

### 治理接口

后台交易治理 REST 接口位于 `TradeOpsController`，路径前缀 `/ops/trade`，涵盖本地消息查询/重投、库存对账、Redis 库存快照刷新和支付日终对账。
