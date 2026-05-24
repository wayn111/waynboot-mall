# 模块拆分演进计划

本文档列出 waynboot-mall 模块边界优化的两条独立路径：B（支付渠道隔离）和 A（领域模块拆分）。每条路径在独立 PR 中推进，互不依赖。

---

## 现状结论（2026-05）

`waynboot-common` 已经从"通用基础设施收敛点"演变成"全栈业务模块"：

- 26 个业务实体（订单、商品、库存、支付、消息、用户、营销）共居一模块
- 7 个 support 子包按领域切分，但只通过 Java 包名隔离，无编译期约束
- 支付渠道 SDK（微信/支付宝/易支付）和编排层在同一模块

短期可运行，长期会持续提高维护成本，并在接入 ShardingSphere、抽离微服务、加新领域（履约、风控）时形成阻力。

本轮 D（util 瘦身）已完成；B 和 A 留作独立 PR。

---

## B：支付渠道隔离（waynboot-payment-channel）

### 目标

把 18 个支付/退款渠道实现 + 微信/支付宝/易支付 SDK 依赖迁出到独立模块，common 只保留接口契约。

### 范围

迁出到 `waynboot-payment-channel`：

```
common/design/strategy/pay/concretestrategy/  (6 文件: WxJsapi/WxH5/AliH5/EPayWx/EPayAli/Test PayStrategy)
common/design/strategy/refund/concretestrategy/ (6 文件: 同上 RefundStrategy)
common/wapper/epay/                             (6 文件: Epayapi + 请求/响应/工具)
```

留在 `waynboot-common`：

```
common/design/strategy/pay/PayTypeEnum               ← 被 Order 实体引用，属于订单领域
common/design/strategy/pay/strategy/PayTypeInterface ← 接口契约
common/design/strategy/pay/context/PayTypeContext    ← Spring 自动注入实现集合
common/design/strategy/refund/strategy/RefundInterface
common/design/strategy/refund/context/RefundContext
```

### 阻塞点：PaymentCallbackSupport 的耦合

`PaymentCallbackSupport.epayPayNotify()` 直接调用了 `EpaySignUtil.sign()` 做验签。如果把 `EpaySignUtil` 迁出，common 就反向依赖 payment-channel。

**先做的设计变更**（B 任务前置）：

抽象 `PaymentChannelVerifier` 接口，每个渠道在 payment-channel 内实现：

```java
// 留在 common
public interface PaymentChannelVerifier {
    PaymentNotifyChannelEnum channel();
    boolean verify(HttpServletRequest request);
    Map<String, String> extractCallbackParams(HttpServletRequest request);
}

// 迁到 payment-channel
@Component
public class EpaySignVerifier implements PaymentChannelVerifier { ... }
```

`PaymentCallbackSupport` 改为通过 `Map<PaymentNotifyChannelEnum, PaymentChannelVerifier>` 注入，按 channel 路由。

### 依赖迁移

`waynboot-payment-channel/pom.xml` 已经独立声明 `weixin-java-pay` 和 `alipay-sdk-java`。

`waynboot-common/pom.xml` 暂时保留这两个 SDK 声明，因为 6 个 common 文件还直接 import SDK 类型：

- `PaymentCallbackSupport`：`com.alipay.api.internal.util.AlipaySignature`、`WxPayNotifyResponse`、`WxPayOrderNotifyResult`、`BaseWxPayResult`、`WxPayService`
- `AdminOrderRefundSupport`：refund 相关 SDK 类型
- `OrderServiceImpl` / `IOrderService` / `OrderPayResVO`：`WxPayUnifiedOrderV3Result.JsapiResult`
- `WxPayServiceConfig`：`WxPayService` Bean 配置

彻底剥离需要后续重构：

1. 把 `OrderPayResVO.jsapiResult` 字段从 `WxPayUnifiedOrderV3Result.JsapiResult` 改为自定义 `WxJsapiPayResult` POJO
2. `PaymentCallbackSupport` 通过 `PaymentChannelVerifier` 接口（B 阶段第二轮重构再做）反向调用 payment-channel 的验签实现
3. `WxPayServiceConfig` 移到 payment-channel
4. 完成上述步骤后才能从 `waynboot-common/pom.xml` 删除两个 SDK 依赖

依赖方向（现状）：

```
admin-api  → common, payment-channel
mobile-api → common, payment-channel
payment-channel → common
```

### 验证清单

- `mvn clean package` 全 12 模块通过
- `PaymentCallbackSupportTest` 6 个测试全绿
- 三种支付渠道回调路径手工 smoke：微信 V3 / 支付宝 / 易支付

### 工作量估算

中等。约 25 个文件移动 + 5 个新接口实现 + pom 调整。一次提交完成。

---

## A：领域模块拆分

### 关键架构决策：waynboot-domain-api 接口模块

迁移 promotion 时撞上循环依赖：promotion 依赖 common（用 ShopBaseEntity），common 中 trade/cart/home 的 support 反向 import promotion 的 entity / Service 接口。Maven reactor 直接报循环依赖错。

破解方案：新建 `waynboot-domain-api` 模块，集中存放跨域使用的契约：

- 跨域使用的 entity（`Channel`、`Diamond`、`ShopCoupon`、`ShopMemberCoupon`）
- 跨域使用的 Service 接口（`IChannelService`、`IDiamondService`、`ShopCouponService`、`ShopMemberCouponService`）
- 跨域使用的 Mapper 接口
- 接口签名涉及的 ReqVO / ResVO

`waynboot-domain-api` 只依赖 `waynboot-util`，不依赖 common 或任何 domain 模块。

依赖图变成：

```
util → domain-api → common → admin-api / mobile-api
                          ↘ domain-promotion → admin-api / mobile-api
                          ↘ domain-trade（待迁）
```

`common` 反向引用 promotion 的部分改为引用 `domain-api`，循环消除。

**前置改动：base entity 迁到 util**

`BaseEntity` 和 `ShopBaseEntity` 从 `com.wayn.common.base.entity` 迁到 `com.wayn.util.entity`。util 加 `mybatis-plus-boot-starter` 和 `jackson-annotations` 依赖。这一步是 domain-api 能独立存在的前提（否则 entity 还要依赖 common）。

### 目标模块结构

```
waynboot-common              ← 基础设施 + 暂未迁出的 trade/cart/goods/inventory/home support
waynboot-domain-api          ← 跨域契约：entity / Service 接口 / Mapper 接口 / 接口涉及的 VO
waynboot-domain-trade        ← 订单 + 支付编排 + 状态机 + 本地消息（待迁）
waynboot-domain-goods        ← 商品 + SKU + ES 同步 + 类目（待迁）
waynboot-domain-cart         ← 购物车（待迁）
waynboot-domain-promotion    ← 优惠券实现 + diamond strategy 实现（已迁）
waynboot-domain-inventory    ← 库存 + 流水 + 对账（待迁）
```

### 已完成（A-2 + A-3 + A-4 + A-5 全部）

- 5 个 domain 模块骨架建好
- `waynboot-domain-api` 接口契约模块建好
- `BaseEntity` / `ShopBaseEntity` 迁到 util
- promotion 完整迁移：4 entity + 4 Mapper + 4 Service 接口 → domain-api；4 ServiceImpl + 5 diamond strategy + 2 coupon support + Mapper XML → domain-promotion
- 6 个 promotion 相关 VO 迁到 domain-api
- **trade/goods/cart/inventory/outbox 全部 23 个 entity 迁到 domain-api**
- **22 个 Mapper 接口迁到 domain-api**
- **35+ 个 Service 接口和命令对象迁到 domain-api**
- **30+ 个跨域 VO/ReqVO/ResVO 迁到 domain-api**
- **6 个跨域枚举（PayTypeEnum、PaymentFlowSaveResult、OrderStatusChangeTypeEnum、InventoryChangeTypeEnum、LocalMessageStatusEnum、LocalMessageFailureReasonEnum）迁到 domain-api**
- **TradeLockSupport / WaynConfig / MybatisPlusTableInfoTestHelper 迁到 domain-api**
- **A-5a：12 个 goods ServiceImpl + 4 个 goods Support + 11 个 Mapper XML 迁到 domain-goods**
- **A-5b：InventoryReconciliationService impl + 7 个 stock support（OrderStockSupport、RedisStockPreDeductSupport 等）+ Mapper XML 迁到 domain-inventory**
- **A-5c：CartServiceImpl + 3 个 cart support + Mapper XML 迁到 domain-cart**
- **A-5d：9 个 trade ServiceImpl + 30+ 个 trade support（含 chain、admin、payment）+ outbox 全套 + 7 个 Mapper XML 迁到 domain-trade**
- **测试代码全部跟随对应实现迁到 domain 模块**
- admin-api / mobile-api 加全部 domain 模块依赖
- `mvn clean package` 20 模块全过；`mvn test` 全模块跑通，**173 个测试全绿**

### 当前架构状态（终态）

```
util → domain-api → domain-promotion → admin-api / mobile-api
                  → domain-goods    →
                  → domain-inventory →
                  → domain-cart     →
                  → domain-trade    →
                  → common (基础设施: base/, config/, annotation/, aspect/, convert/, constant/)
                  → payment-channel
```

`domain-api` 收敛了所有跨域契约（entity / Service 接口 / Mapper 接口 / VO / 枚举 / 通用工具）。每个 domain-X 模块持有自己领域的实现（ServiceImpl + Support + Mapper XML），单向依赖 domain-api。common 只剩基础设施类，不再持有业务实现。

### 测试数分布

| 模块 | 测试数 |
|---|---|
| domain-trade | 103 |
| domain-inventory | 23 |
| domain-goods | 17 |
| mobile-api | 12 |
| message-consumer | 8 |
| domain-cart | 5 |
| domain-api（TradeLockSupport） | 3 |
| common（TradeTableShardRouter） | 1 |
| domain-promotion | 1 |
| **合计** | **173** |

### 仍可优化（独立小 PR）

- common 中 `WxPayServiceConfig` 和支付 SDK 引用可以最终剥离到 payment-channel（详见 B 章节末尾）
- common pom 现在 transitive 依赖 domain-inventory/goods（因为 common 中尚有少量 trade 编排代码引用了 inventory/goods）；如果把 common 中残留的支付编排相关 OrderUtil/OrderHandleOption 整理到 trade 模块，可彻底剥离这条依赖
- domain-api 中 `IOrderService` 接口签名带 `WxPayException`/`AlipayApiException` 是设计气味，应包装成自定义异常



### 实体划分

| 实体 | 目标模块 |
|---|---|
| Order, OrderGoods, OrderStatusLog | trade |
| PaymentFlow, PaymentChannelBill, PaymentRefundFlow | trade |
| Goods, GoodsProduct, GoodsAttribute, GoodsSpecification, Category, Channel, Banner, Column, ColumnGoodsRelation, Comment, Keyword, SearchHistory | goods |
| Cart | cart |
| ShopCoupon, ShopMemberCoupon, Diamond | promotion |
| InventoryFlow | inventory |
| Address, Member | system 或独立 user 模块（待定） |
| LocalMessage*, OrderStatusLog | trade（trade 拥有交易侧 outbox） |

### 跨域引用清单（待验证）

需要在执行前盘清，候选：

- `OrderSubmitSupport` → `ICartService`、`ShopMemberCouponService`、`InventoryFlowService`、`GoodsProductService`：跨 trade/cart/promotion/inventory/goods
- `PaymentCallbackSupport` → `OrderStatusLogService`、`InventoryFlowService`：跨 trade/inventory
- `OrderCancellationSupport` → `OrderStockSupport` (库存回补)：跨 trade/inventory
- `GoodsMutationSupport` → ES 同步：goods 内闭

跨域引用通过 Service 接口传递，禁止跨域直接 import Support 类。

### 执行顺序

1. **A-1 盘点**：grep 跨域引用，输出 `cross-domain-imports.txt`
2. **A-2 骨架**：创建 5 个空 module 的 pom + 包扫描占位
3. **A-3 试点**：先迁 `cart` 或 `promotion`（最小领域，跨域引用少），验证 Mapper XML 路径、`@MapperScan`、`typeAliasesPackage`、测试 mock 路径全部跑通
4. **A-4 主体**：迁 `trade`（核心领域，跨域引用最多）
5. **A-5 收尾**：迁 `goods` + `inventory`，common 收回到只剩基础设施

每步独立 PR，每步走完都能 `mvn clean package` 通过。

### 风险

- Mapper XML 路径变更需要同步调整 `mybatis-plus.mapperLocations`
- `typeAliasesPackage: com.wayn.**.domain` 需要扫描多模块路径
- 测试 mock 路径需要更新（特别是 `Wrappers.lambdaQuery` 涉及 TableInfo 的初始化逻辑）
- 如果两个 domain 之间发现循环依赖，需要先抽 `domain-shared` 或调整 Service 接口位置

### 工作量估算

大。约 4 周分 4-5 个 PR 推进。建议先完成 B 再启动 A。

---

## 执行约束

- 任何一步失败不影响其他步骤的回滚
- 每个 PR 独立通过 `mvn clean package` + `mvn test -pl waynboot-common`
- 不与日常业务功能 PR 混合提交
- A 的试点（A-3）必须把"模板模式"完整跑通后再启动 A-4 / A-5
