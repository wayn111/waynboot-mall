# 商品与管理端订单第一阶段重构说明

## 1. 最近一次 Git 提交背景

当前仓库最近一次已提交的 Git 记录是：

- 提交哈希：`ff1ed694b7fd448c465cfc83c31cfa0d3319239c`
- 提交时间：`2026-04-09 23:43:02 +0800`
- 提交人：`wayn`
- 提交标题：`优化商城模块逻辑并修复编码问题`

这次提交共涉及 `111` 个文件，整体变更规模为：

- `502` 行新增
- `453` 行删除

从提交范围看，这次提交本质上是一次“模型包迁移 + 入口契约收口 + 编码治理 + 局部业务修补”的大整理，为后续继续拆分商品域和交易域实现类打基础。

它优先解决的是下面几类问题：

- `dto`、`request`、`response` 包路径散落，接口模型边界不清
- Controller、Service、Mapper 之间导入路径不统一，编译时容易出现找不到类
- 部分文本文件和 Maven 编译配置没有把 UTF-8 规则彻底收口，容易引出乱码和构建问题
- 个别控制器直接持有 Redis / ES 依赖，入口层职责偏重
- 商品、购物车等链路里已经存在一些可读性和空值安全问题

但这次提交还没有把商品域和交易域的大实现类彻底拆开，像 `GoodsServiceImpl`、`OrderServiceImpl` 这类核心类虽然完成了接口迁移和局部整理，职责仍然偏重，因此后续才继续推进本轮 Phase1 的 facade + support 拆分。

## 2. 最新一次提交的代码变更总览

### 2.1 工程治理与编码基线

最新提交先在工程层面补齐了编码与协作基线：

- 新增 `.editorconfig`
  - 统一 `java`、`xml`、`yml`、`yaml`、`properties`、`md` 的 `utf-8` 编码
  - 统一使用 `crlf`
  - 统一插入文件末尾换行
  - 统一 4 空格缩进
- 调整根 `pom.xml`
  - 补充 `project.reporting.outputEncoding=UTF-8`
  - 为 `maven-compiler-plugin` 显式指定 `encoding`
  - 顺手规范了部分依赖注释和排版
- 新增仓库级 `AGENTS.md`
  - 收口模块划分、分层约束、测试和提交流程

这部分改动的维护意义很直接：

- 先把“源码文件怎么存”“Maven 怎么按 UTF-8 编译”固定下来
- 后续再做大规模包迁移和 Java 文件清理时，不容易继续引入新的乱码和 BOM 问题

### 2.2 公共模型层迁移

这次提交把原来散落在下面几个包里的接口模型：

- `com.wayn.common.dto`
- `com.wayn.common.request`
- `com.wayn.common.response`

统一迁移到了：

- `com.wayn.common.model.dto`
- `com.wayn.common.model.request`
- `com.wayn.common.model.response`

典型对象包括：

- 订单管理：`OrderManagerReqVO`、`OrderRefundReqVO`、`ShipRequestVO`、`OrderManagerResVO`、`OrderDetailResVO`
- 移动端交易：`OrderCommitReqVO`、`OrderPayReqVO`、`OrderListResVO`、`SubmitOrderResVO`、`OrderStatusCountResVO`
- 登录与用户：`GenMobileCodeReqVO`、`CaptchaResVO`、`ProfileRequestVO`、`UserProfileResVO`
- 商品与优惠券：`GoodsSaveRelatedReqVO`、`ShopCouponReqVO`、`CouponReceiveReqVO`、`ShopCouponResVO`

这一步是本次提交最核心的“契约层整理”：

- DTO / ReqVO / ResVO 不再混在 `common` 根包下
- 业务实体和接口出参的角色划分更明确
- 后续 Controller、Service、Mapper、策略类都可以围绕 `common.model.*` 统一对齐

这也是此前 `LoginController` 编译报错里 `GenMobileCodeReqVO` 找不到的根本原因所在。最新提交通过统一迁移和批量修正导入，解决了这一类符号找不到问题。

### 2.3 入口层契约收口

`waynboot-admin-api` 和 `waynboot-mobile-api` 在这次提交里做了两类收口：

- 第一类是批量导入修正
  - 所有 Controller 改为依赖 `common.model.request` / `common.model.response`
  - 避免继续引用旧的 `common.request` / `common.response`
- 第二类是少量真实入口职责优化
  - 管理端 `GoodsController` 不再直接持有 `ElasticDocument`、`RedisLock`、`RedisCache`
  - 商品 ES 全量同步改为走 `IGoodsService.syncGoodsToEs()`
  - 商品详情接口返回值由 `Map<String, Object>` 收口为强类型 `GoodsManageDetailResVO`

这一层的价值是：

- Controller 只保留权限、参数校验、接口编排和响应封装职责
- ES / Redis 这类中间件逻辑重新回收到 Service，减少入口层耦合
- 后台商品详情出参从“弱类型 Map 拼装”转成“可扩展 VO”，后续新增字段更容易维护

### 2.4 商品域服务的真实行为变更

虽然最新提交的大部分改动属于模型迁移，但商品域里已经出现了一批真实行为调整，主要集中在 `IGoodsService` 和 `GoodsServiceImpl`。

#### 2.4.1 服务契约调整

- `IGoodsService.getGoodsInfoById(Long goodsId)` 的返回值从 `Map<String, Object>` 改成 `GoodsManageDetailResVO`
- `IGoodsService` 新增 `syncGoodsToEs()`，把原先控制器里的 ES 全量同步逻辑收回服务层

#### 2.4.2 商品详情结构收口

新增 `GoodsManageDetailResVO`，统一承载：

- 商品基础信息 `goods`
- 规格集合 `specifications`
- 货品集合 `products`
- 属性集合 `attributes`
- 分类路径 `categoryIds`
- 当前分类对象 `category`

同时，`GoodsServiceImpl.getGoodsInfoById` 补了空值校验：

- 商品不存在时直接抛业务异常
- 避免后续分类、规格、属性拼装时继续空指针向下传播

#### 2.4.3 商品保存与更新链路补强

`GoodsServiceImpl` 在保存和更新商品时，新增了几段显式校验和辅助方法：

- `validateProducts`
  - 货品列表为空时直接阻断
- `resolveRetailPrice`
  - 从货品价格中计算最低零售价，统一回写主表 `retailPrice`
- `validateDefaultSelected`
  - 默认选中 SKU 超过一个时直接阻断
  - 从直接 `stream().filter(GoodsProduct::getDefaultSelected)` 改成 `Boolean.TRUE.equals(...)`
  - 避免默认值为空时触发空指针

这部分说明最新提交虽然还没有拆成 support 类，但已经开始把“可读性差、重复出现的局部规则”从大方法体里往外收口。

#### 2.4.4 商品 ES 同步逻辑回收到服务层

`syncGoodsToEs()` 在服务层完成了几件事：

- 通过 Redis 锁串行化 ES 全量同步
- 删除原有索引并重新创建索引
- 校验 ES 索引模板文件是否存在
- 批量读取商品并构造 `ElasticEntity`
- 批量写入商品索引

配套还补充了：

- `buildGoodsElasticEntity`
  - 统一单商品和批量同步时的 ES 文档结构
- `resolveKeywords`
  - 对关键字分词结果做空值、空白过滤和裁剪
- `syncGoods2Es`
  - 单商品同步改为复用统一文档构造逻辑

相比旧实现，这里有两个直接收益：

- ES 字段构造不再散落在控制器里
- 单商品同步与全量同步共享同一套文档格式，减少后续搜索字段不一致的问题

### 2.5 购物车与周边链路的补丁式修复

这次提交里，`CartServiceImpl` 除了模型导入迁移，还做了几处真实问题修正：

- 加入商品和货品空值校验
  - 商品不存在时直接阻断
  - 默认货品列表为空时直接阻断
- 修复购物车已有记录更新时错误设置对象的问题
  - 原逻辑更新数量后写的是 `cart.setUpdateTime(...)`
  - 新逻辑修正为 `existsCart.setUpdateTime(...)`
- `changeNum` 不再通过原始 `setSql("number = ...")` 更新
  - 改成显式字段更新
  - 同时补上 `updateTime`
  - 先做 `cartId`、`number` 合法性校验
- 默认货品选择改成 `Boolean.TRUE.equals(product.getDefaultSelected())`
  - 避免空值布尔字段带来的空指针风险

这部分改动说明最新提交并不只是“改包名”，也顺手修掉了几个已有链路里的安全性和可读性问题。

此外，以下模块主要属于“契约跟随式调整”：

- `CategoryServiceImpl`
- `GoodsDetailServiceImpl`
- `IHomeServiceImpl`
- `PayServiceImpl`
- `ShopCouponServiceImpl`
- 支付策略、退款策略、钻石跳转策略相关接口和实现

它们的主要目的，是把依赖的请求/响应对象同步迁移到 `common.model.*`，让整个调用链导入关系保持一致。

### 2.6 订单与 Mapper 契约对齐

订单和优惠券相关改动，主要体现在 Mapper、Mapper XML、Service 接口与 Controller 四个位置：

- `AdminOrderMapper`、`ShopCouponMapper`
  - 改为依赖 `common.model.request` 和 `common.model.response`
- `AdminOrderMapper.xml`、`ShopCouponMapper.xml`
  - `resultType` 从旧包路径改到 `common.model.response`
- `IOrderService`、`IMobileOrderService`、`ShopCouponService`
  - 方法签名对齐到新模型包
- 管理端、移动端相关 Controller
  - 全量切换到新的请求和响应对象路径

这一层虽然业务行为变化不大，但对维护非常关键：

- Mapper 查询结果类型和 Java 接口签名终于统一
- 后续如果继续加字段，不需要一边在旧包改、一边在新包补
- 避免“代码能编译，但 XML 还在指旧类”的隐性问题

## 3. 本轮 Phase1 与最新提交的关系

本文件描述的“商品与管理端订单第一阶段重构”，是在上面这次 Git 提交基础上的继续收口，重点不是再次做包迁移，而是把已经完成迁移后的业务实现继续拆清楚。

可以把两者理解为上下两层：

- 最新 Git 提交
  - 解决“模型包结构、编码问题、接口契约统一、入口层轻量化”的问题
  - 让仓库先回到可持续演进的基础状态
- 本轮 Phase1
  - 解决“商品与订单核心实现职责过重、并发状态不安全、聚合更新不完整”的问题
  - 让交易链路和商品链路进入更适合维护和扩展的结构

换句话说，最新提交是“先把地基和接口面整理平”，本轮 Phase1 是“开始把核心业务实现真正拆开”。

## 4. 本轮 Phase1 的背景

本轮优化聚焦 `waynboot-common` 中商品域和管理端订单域的核心服务实现。改造前，`GoodsServiceImpl` 和 `OrderServiceImpl` 同时承担查询、校验、写入、状态迁移、ES 同步、三方退款编排等多类职责，存在以下问题：

- 单类职责过多，方法体过长，维护成本高
- 交易状态迁移缺少统一的条件更新约束，并发下容易出现重复处理或状态覆盖
- 商品聚合更新对规格、属性、货品的删除场景收口不足，容易留下脏数据
- ES 同步逻辑和商品写路径耦合在一起，不利于后续扩展为单商品同步、重试或异步化

## 5. 本轮 Phase1 的目标

- 保持外部接口、请求参数、响应结构和 Spring 注入点兼容
- 把商品域和订单域的大实现类收口为 facade，只保留对外编排职责
- 把查询、校验、聚合写入、状态迁移、ES 同步拆分为独立 support 类
- 优先增强并发安全、状态一致性和后续扩展性

## 6. 结合最新提交后的优化逻辑整理

把最新提交和本轮 Phase1 放在一起看，商品域与订单域的演进逻辑可以整理为下面几条主线。

### 6.1 模型层先统一，再继续收口业务层

最新提交先把请求、响应、DTO 统一迁移到 `common.model.*`，这一步的价值是：

- 对外接口契约不再散落在多个包路径
- Controller、Service、Mapper 之间的依赖边界更清晰
- 后续做 facade/support 拆分时，不需要再同时处理模型迁移问题

本轮 Phase1 则在这个基础上继续推进，把 facade 和 support 结构建立起来，避免“模型已经规范了，但实现类仍然是一团”的情况。

### 6.2 商品域从“强类型详情 + 局部规则收口”继续走向“聚合写入 + 查询分离”

最新提交已经完成了这些动作：

- 后台商品详情出参从 `Map<String, Object>` 收口为 `GoodsManageDetailResVO`
- 商品 ES 全量同步从 Controller 回收到 Service
- 商品保存和更新链路开始提炼公共校验和价格计算逻辑

本轮 Phase1 则继续向前推进：

- 查询逻辑下沉到 `GoodsQuerySupport`
- 写入逻辑下沉到 `GoodsMutationSupport`
- 校验逻辑下沉到 `GoodsValidationSupport`
- ES 同步逻辑下沉到 `GoodsElasticSyncSupport`

这样串起来看，商品域的演进顺序是：

1. 先把返回结构和同步入口收成强类型、可控边界
2. 再把查询、校验、写入、搜索同步从大实现类里彻底拆开

### 6.3 订单域先完成契约层统一，再进入状态迁移收口

最新提交已经统一了订单相关请求 / 响应模型，并把订单接口签名、控制层导入、Mapper 查询输出都迁移到了新的模型层。

本轮 Phase1 继续把订单逻辑拆成：

- `AdminOrderQuerySupport`
- `AdminOrderRefundSupport`
- `AdminOrderShipmentSupport`

这一步最核心的优化不是“类拆小了”，而是把状态流转规则显式化了：

- 退款必须带当前状态条件更新
- 发货必须带当前状态条件更新
- 退款成功和退款失败的库存处理逻辑不再混在一个模糊分支里

### 6.4 编码问题修复之后，继续补并发与一致性

最新提交里“修复编码问题”解决的是可编译性和代码管理问题。

本轮 Phase1 解决的是业务一致性问题：

- 商品更新时子项删除不再遗漏
- 退款不会在并发下重复覆盖状态
- 发货不会在并发下重复推进订单状态
- ES 重建不会多实例同时执行

所以两轮工作的关系很明确：

- 上一轮先修“代码能稳定维护”
- 这一轮再修“业务能稳定运行”

## 7. 本轮 Phase1 改造范围

本轮只覆盖以下实现：

- 商品域
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/impl/GoodsServiceImpl.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/GoodsValidationSupport.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/GoodsQuerySupport.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/GoodsMutationSupport.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/GoodsElasticSyncSupport.java`
- 管理端订单域
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/impl/OrderServiceImpl.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/AdminOrderQuerySupport.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/AdminOrderRefundSupport.java`
  - `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/AdminOrderShipmentSupport.java`

## 8. 本轮 Phase1 设计说明

### 8.1 商品域

- `GoodsServiceImpl`
  - 作为 facade 保留 `IGoodsService` 现有公开契约
  - 不再直接承载商品详情聚合、商品写入、校验和 ES 同步实现

- `GoodsValidationSupport`
  - 负责商品名唯一性、货品非空、默认 SKU 唯一、最低零售价解析
  - 保证保存和更新路径使用同一套校验规则

- `GoodsQuerySupport`
  - 负责商品分页、栏目商品分页、首页商品、商品管理详情查询
  - 把商品、分类、规格、属性、货品聚合为管理端可直接消费的结构

- `GoodsMutationSupport`
  - 负责商品聚合保存、更新、删除
  - 保存和更新统一以货品最低售价回写主表 `retailPrice`
  - 更新时对规格、属性、货品执行增删改对账，前端移除的子项会同步删除

- `GoodsElasticSyncSupport`
  - 负责商品索引配置读取、全量索引重建、单商品文档同步和文档删除
  - 全量同步使用 Redis 锁串行化，避免多实例同时删索引和重建索引

### 8.2 管理端订单域

- `OrderServiceImpl`
  - 作为 facade 保留 `IOrderService` 现有公开契约
  - 不再直接混合承载查询、退款、发货逻辑

- `AdminOrderQuerySupport`
  - 负责订单列表、订单详情聚合
  - 统一补齐订单状态、退款状态、支付方式、退款方式等展示文案

- `AdminOrderRefundSupport`
  - 负责退款前校验、三方退款执行、退款结果落库和库存回补
  - 三方退款失败时，收口为退款失败状态并落库，不直接把三方异常扩散为未落库状态
  - 订单退款更新使用“当前状态 = 申请退款”的条件更新，避免并发下重复退款覆盖状态
  - 只有三方退款确认成功时才回补库存，避免错误增加可售库存

- `AdminOrderShipmentSupport`
  - 负责发货前校验和发货状态更新
  - 发货更新使用“当前状态 = 已支付”的条件更新，防止重复发货

## 9. 并发与一致性增强点

- 商品更新链路现在会删除已经被前端移除的规格、属性和货品，避免聚合数据残留
- 管理端退款和发货都通过当前状态条件更新控制状态迁移，降低并发覆盖风险
- 商品 ES 全量同步增加锁控制，避免多实例同时重建索引
- 退款成功才回补库存，退款失败只记录失败状态和失败原因，不做错误库存回补

## 10. 后续维护整理

为了让后续维护者快速判断一处改动应该怎么接，这里把最新提交和本轮 Phase1 统一整理成下面几条维护口径。

### 10.1 先判断改动属于哪一层

维护时先区分三类改动：

- 契约层改动
  - 例如 ReqVO / ResVO / DTO 增删字段
  - 这类改动要同步检查 Controller、Service、Mapper、Mapper XML 是否一起跟上
- 行为层改动
  - 例如商品保存、购物车数量变更、退款和发货状态流转
  - 这类改动要优先检查事务、空值、并发和幂等语义
- 结构层改动
  - 例如 facade + support 拆分、ES 同步职责迁移
  - 这类改动要优先保证外部接口不变，再逐步下沉内部实现

### 10.2 接口模型统一放在 `common.model.*`

后续新增或调整接口对象时，统一遵守：

- 请求对象放 `com.wayn.common.model.request`
- 响应对象放 `com.wayn.common.model.response`
- DTO 放 `com.wayn.common.model.dto`

不要再往下面几个旧包继续新增类：

- `com.wayn.common.request`
- `com.wayn.common.response`
- `com.wayn.common.dto`

如果再次出现“找不到符号”一类问题，第一检查项就是导入路径是否还指向旧包。

### 10.3 入口层不要重新回退成“胖 Controller”

这次最新提交已经证明了一个方向：

- `GoodsController` 不应该再自己持有 Redis / ES 依赖
- 商品详情也不应该继续返回 `Map<String, Object>`

后续新增接口时建议继续保持：

- Controller 只做鉴权、参数校验、调用编排、统一返回
- 搜索、缓存、分布式锁、聚合拼装尽量收回 Service 或 support 层
- 如果出参是一个稳定结构，优先定义强类型 VO，不要回退成弱类型 `Map`

### 10.4 商品域维护口径

商品域后续维护时，优先遵守下面几条：

- 后台商品详情继续基于 `GoodsManageDetailResVO` 扩展，不要再拼裸 `Map`
- 商品保存和更新都必须保证：
  - 货品列表不能为空
  - `retailPrice` 由货品最低价计算得出
  - 默认选中 SKU 只能有一个
- ES 全量同步统一经由服务层或 support 层处理
  - 不要把 Redis 锁、索引模板读取、批量写入重新塞回 Controller
- ES 文档字段结构尽量复用统一构造方法
  - 避免全量同步和单商品同步字段不一致

### 10.5 订单与优惠券链路维护口径

订单和优惠券相关维护时，优先检查：

- Mapper Java 接口的入参 / 出参包路径是否和 Mapper XML 的 `resultType` 保持一致
- 订单状态迁移是否具备显式的当前状态约束
- 涉及退款、发货、库存回补的逻辑是否仍然保持幂等和条件更新语义

这部分虽然在最新提交里主要还是契约迁移，但它直接决定了本轮 Phase1 的 support 化拆分能否顺利继续。

### 10.6 编码与构建维护口径

后续维护文本文件和构建配置时，统一遵守：

- 代码和配置文件统一使用 UTF-8
- Maven 构建链路不要移除显式编码配置
- 新增模块或脚本文件时，尽量遵守 `.editorconfig`
- 出现乱码、注释异常或 XML 映射读取异常时，优先排查文件编码而不是先怀疑业务代码

### 10.7 继续推进 Phase2 / Phase3 时的建议顺序

如果继续推进交易链路重构，建议按下面顺序做：

1. 先保持 Controller 和 Service 对外契约稳定
2. 再把实现类里的查询、校验、状态迁移、外部适配逻辑逐段拆到 support
3. 最后再考虑异步化、MQ 化、重试队列和更细粒度的并发治理

这样可以避免在同一轮改动里同时做“契约迁移 + 大规模逻辑重写 + 并发模型调整”，降低风险。

## 11. 注释补充策略

本轮对上述 facade / support 类补齐了：

- 类级职责注释
- 方法级注释
- 关键分支注释

重点说明：

- 事务边界
- 条件更新语义
- 聚合对账逻辑
- Redis 锁和 ES 同步目的
- 退款失败为何仍要落退款失败状态

## 12. 测试覆盖

新增或补充的关键测试包括：

- `GoodsValidationSupportTest`
- `GoodsMutationSupportTest`
- `GoodsElasticSyncSupportTest`
- `AdminOrderQuerySupportTest`
- `AdminOrderRefundSupportTest`
- `AdminOrderShipmentSupportTest`

验证重点包括：

- 商品名重复校验
- 默认 SKU 唯一性
- 最低零售价计算
- 商品子项增删改对账
- ES 加锁和同步失败语义
- 退款失败状态落库
- 退款条件更新失败时阻断重复处理
- 发货条件更新失败时阻断重复发货

## 13. 非目标

本轮没有处理以下内容：

- `application*.yml` 配置文件
- 商品写入后自动触发单商品 ES 同步策略调整
- 移动端和后台 Controller 层的进一步收口
- MQ、Job、Monitor 等模块的结构整理

## 14. 后续建议

- 在商品写入链路上继续评估“单商品 ES 同步”与“异步重试队列”的拆分
- 为商品和订单 support 层补充更多失败分支测试，例如 ES 创建失败、库存回补失败、多线程竞争条件更新等
- 延续 facade + support 的模式，逐步收口其他模块的大实现类

## 15. 本轮继续执行 1 / 2 / 3 的收口结果

本轮是在前述 Phase1 基础上，继续把之前规划的 3 个方向落完：

1. `mobile-api` / `admin-api` Controller 继续 VO 化并补日志
2. 交易链路 Phase2 收口，继续增强并发与扩展性
3. 补齐服务层和失败分支测试

### 15.1 Controller 层继续收口

本轮重点继续清理入口层“参数不清晰、日志不足、返回弱类型结构”的问题，覆盖了移动端与管理端中和商品、购物车、订单、支付、优惠券直接相关的核心控制器。

主要动作包括：

- 移动端购物车接口改为显式请求对象
  - 新增 `CartAddReqVO`
  - 新增 `CartUpdateReqVO`
- 管理端和移动端部分接口继续改成强类型返回
  - 新增 `ExpressVendorResVO`
  - 新增 `ShopCouponManageResVO`
  - 新增 `GoodsManageListItemResVO`
  - 购物车结算返回中的明细结构下沉为 `CartCheckedItemResVO`
- 入口层统一补日志
  - 记录用户 ID、订单号、分页参数、关键业务参数
  - 对支付回调、登录、购物车、地址、搜索、用户信息等关键入口补充开始 / 完成日志
  - 避免打印密码、token、支付验签明文、验证码等敏感信息

这一步的目标不是“把 Controller 写得更热闹”，而是把维护入口统一成下面三条：

- 入参必须清晰
- 返回必须 VO 化
- 异常链路必须可追踪

### 15.2 购物车链路继续拆读写职责

购物车链路本轮继续沿着 facade + support 拆分：

- `CartServiceImpl`
  - 退化为对外 facade，只保留接口编排职责
- `CartWriteSupport`
  - 负责加购、默认货品加购、勾选更新、数量修改
  - 所有写操作统一走用户维度锁，减少并发覆盖
- `CartReadSupport`
  - 负责购物车列表和已勾选商品结算汇总
  - 读取时会同步修正失效或库存不足的勾选项

其中几个关键调整如下：

- 购物车勾选状态更新改为显式 `updateChecked(cartId, checked, userId)`，不再依赖直接传完整实体
- 已勾选结算逻辑从 Controller 下沉到 `CartReadSupport`
- 读取结算信息时，如果勾选项已失效或库存不足，会同步取消勾选，避免继续参与下单
- 当所有勾选项都失效时，本轮额外修正为：
  - 不再错误计算运费
  - 不再继续查询无意义的优惠券门槛匹配
- 优惠券筛选增加 `expireTime`、`min` 空值保护，减少历史脏数据导致的空指针

### 15.3 交易链路 Phase2 收口

本轮交易链路继续围绕“幂等、防重、状态条件更新、补偿收口”推进，重点集中在支付回调、退款和取消订单补偿。

#### 15.3.1 支付回调统一幂等入口

`PaymentCallbackSupport` 本轮继续统一微信、支付宝、易支付三类回调的后半段逻辑：

- 渠道侧只负责验签和参数解析
- 订单状态更新统一走 `markOrderPaid(...)`
- 成功后置动作统一走 `PaymentPostActionSupport`

关键收口点：

- 对微信回调金额继续校验，金额不一致直接失败
- 订单已支付时直接返回“已处理”，不重复推进成功逻辑
- 订单状态更新统一使用“当前状态 = 待支付”的条件更新
- 条件更新失败后会再次查询最新订单状态
  - 如果已经被其他线程处理成已支付，则按成功幂等返回
  - 如果仍未支付，则明确返回失败

这一步使支付回调从“只靠接口重试碰运气”变成了“状态条件更新 + 二次确认”的稳定幂等模型。

#### 15.3.2 退款库存回补继续收口

`AdminOrderRefundSupport` 本轮继续把退款成功后的库存回补统一接到 `OrderStockSupport`：

- 不再直接在退款支撑类里逐个调用 `IGoodsProductService.addStock(...)`
- 统一改为走 `OrderStockSupport.restoreStock(...)`

这样带来的好处有两点：

- 库存回补逻辑不再散落在多个订单分支里
- 后续如果要在库存回补上加统一日志、监控、失败补偿或批量优化，只需要改一个支撑类

同时仍然保持核心语义不变：

- 三方退款成功才回补库存
- 三方退款失败时记录退款失败状态和原因，但不回补库存
- 退款状态更新使用当前状态条件更新，避免重复退款覆盖

#### 15.3.3 取消订单补偿继续保持“状态成功后再补偿”

`OrderCancellationSupport` 继续保持下面这个顺序：

1. 先获取订单取消锁
2. 再做“待支付 -> 关闭状态”的条件更新
3. 只有状态更新成功后才做库存回补和优惠券回退

这一步的意义是：

- 支付回调和超时关闭并发时，不会因为重复取消而多次回库
- 订单已经不是待支付状态时，取消补偿自动短路，不再误回库存和优惠券

### 15.4 本轮新增测试覆盖

本轮在 `waynboot-common` 继续补了交易链路关键 support 的失败分支和并发语义测试，新增或扩展了：

- `CartReadSupportTest`
- `CartWriteSupportTest`
- `PaymentCallbackSupportTest`
- `AdminOrderRefundSupportTest`
- `OrderLifecycleSupportTest`
- `OrderCancellationSupportTest`
- `MybatisPlusTableInfoTestHelper`

重点验证点包括：

- 购物车勾选更新会走用户维度锁
- 已勾选商品结算时会剔除失效项并同步取消勾选
- 所有勾选项失效时，运费和总价都回到 0
- 支付回调金额不一致时直接失败
- 支付回调条件更新失败但最新状态已支付时，按幂等成功处理
- 支付回调条件更新失败且最新状态仍未支付时，明确失败
- 退款三方失败时记录退款失败状态但不回补库存
- 退款成功时会统一调用库存回补支撑
- 退款、确认收货、删除订单在条件更新 / 删除失败时会阻断
- 取消订单条件更新失败时不会触发库存和优惠券补偿

这里额外补了一个测试基础设施 `MybatisPlusTableInfoTestHelper`，专门用于纯单测场景初始化 MyBatis-Plus 的实体元数据缓存。原因是 support 层大量使用 `Wrappers.lambdaQuery / lambdaUpdate`，如果单测不初始化表信息，会被 `lambda cache` 异常打断，掩盖真实业务分支。

### 15.5 本轮验证结果

本轮已完成的构建与验证包括：

- `mvn -q -pl waynboot-common -am -DskipTests compile`
- `mvn -q -pl waynboot-mobile-api,waynboot-admin-api -am -DskipTests compile`
- `mvn -q -pl waynboot-common -am -Dtest=AdminOrderRefundSupportTest,PaymentCallbackSupportTest,CartWriteSupportTest,CartReadSupportTest,OrderLifecycleSupportTest,OrderCancellationSupportTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- `mvn -q test`

最终结果：

- 公共模块编译通过
- 移动端与管理端入口模块编译通过
- 新增和扩展的关键单测通过
- 全仓 `mvn test` 通过

## 16. 本轮高并发下单与商品链路优化

本轮继续从高并发角度收口下单、库存和商品更新链路，重点解决重复提交、MQ 重投、同 SKU 多购物车行扣减，以及管理端商品编辑覆盖实时库存的问题。

### 16.1 下单入口增加短时幂等

`OrderSubmitSupport.asyncSubmit(...)` 在完成地址、购物车、优惠券和金额校验后，会基于下面信息生成下单指纹：

- 用户 ID
- 地址 ID
- 用户优惠券 ID
- 购物车明细快照，包括 `cartId`、`productId`、`number`

指纹会写入 Redis `ORDER_SUBMIT_DEDUP_KEY`，并通过 `RedisCache.setCacheObjectIfAbsent(...)` 原子写入，保证并发重复点击时只有第一个请求真正生成订单号并投递 MQ。

如果后续请求命中同一个指纹：

- 不再重复投递下单 MQ
- 直接返回第一次生成的 `orderSn`
- 保持 `actualPrice` 按当前校验上下文返回，方便前端继续轮询下单结果

这一步主要防止移动端重复点击和网关重试在短时间内打出多条相同下单消息。

### 16.2 落单消费按订单号加锁并二次查重

`OrderSubmitSupport.submit(...)` 现在会使用 `ORDER_SUBMIT_LOCK` 对订单号加锁，真正落单逻辑下沉到锁内执行。
落单事务改为通过 `TransactionTemplate` 在锁内部提交，避免 `@Transactional` 外层代理导致“锁先释放、事务后提交”的极端并发窗口。

锁内首先按 `orderSn` 查询订单是否已经存在：

- 已存在：认为该消息已经处理过，直接跳过库存扣减、订单插入、订单商品插入和优惠券占用
- 不存在：继续走构建上下文、扣库存、插订单、插订单商品、清购物车、占优惠券、投递超时关单消息

这一步解决的是 MQ 重投、回调重试、消费者重复消费时的幂等问题，核心原则是：同一个订单号只能成功落库一次，不能重复扣库存。

### 16.3 库存扣减按 SKU 聚合

`OrderStockSupport.reduceStock(...)` 不再逐条购物车明细直接扣减，而是先按 `productId` 聚合本次下单需要扣减的数量。

这样做有两个直接收益：

- 同一个 SKU 出现在多个购物车行时，只执行一次数据库条件扣减，减少并发更新次数
- 库存不足时按聚合后的总需求判断，避免先扣一部分再失败带来的补偿压力

底层 `GoodsProductMapper.reduceStock(...)` 同时补上 `update_time = now()`，并把条件收口为 `number >= #{number}`，语义更直接。

### 16.4 商品更新改为库存差量调整

管理端更新商品时，已有 SKU 的库存不再通过 `updateBatchById(...)` 直接覆盖。

`GoodsMutationSupport.syncProducts(...)` 会先读取数据库当前货品，再计算本次提交库存与当前库存的差量：

- 差量大于 0：调用 `addStock(...)` 增量补库存
- 差量小于 0：调用 `reduceStock(...)` 条件扣库存
- 差量等于 0：不触发库存更新

随后再把待更新货品对象的 `number` 置空，只更新价格、图片、默认选中、规格等非库存字段。

这一步是为了避免下面的并发覆盖：

1. 管理端打开商品编辑页，看到库存为 10
2. 用户并发下单扣减 3，数据库库存变成 7
3. 管理端保存商品，如果直接覆盖库存为 10，就会把已经售出的 3 件库存写回来

改成差量调整后，管理端的库存修改会以“在当前库存基础上增减”的方式落库，不覆盖下单实时扣减结果。

### 16.5 本轮新增测试覆盖

本轮新增或扩展了以下测试：

- `OrderSubmitSupportTest`
  - 重复下单指纹命中时复用已有 `orderSn`，不重复投递 MQ
  - 已存在订单号再次消费时直接跳过，不扣库存、不插订单、不插订单商品
- `OrderStockSupportTest`
  - 同一个 `productId` 来自多条购物车明细时，先聚合数量再扣减库存
- `GoodsMutationSupportTest`
  - 已有货品更新时库存走差量条件调整
  - 非库存字段更新时不会把页面提交的库存数直接覆盖到数据库

### 16.6 本轮验证结果

本轮执行并通过的验证命令：

- `mvn -q -o -nsu -pl waynboot-common -am -Dtest=OrderSubmitSupportTest,OrderStockSupportTest,GoodsMutationSupportTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- `mvn -q -o -nsu -pl waynboot-common -am -DskipTests compile`
- `mvn -q -o -nsu -pl waynboot-mobile-api,waynboot-admin-api -am -DskipTests compile`
- `mvn -q -o -nsu -pl waynboot-common -am test`
- `mvn -q -o -nsu test`

其中 `-o -nsu` 是为了在当前受限终端里避免 Maven 写入本机仓库更新状态文件；不改变代码验证范围。
