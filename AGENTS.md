# Repository Guidelines

## 项目结构与模块划分
本仓库是基于 Spring Boot 3、Java 17 的 Maven 多模块商城后端。`waynboot-admin-api` 提供后台管理接口（同时承载 Spring `@Scheduled` 治理定时任务），`waynboot-mobile-api` 提供 H5/移动端接口，`waynboot-message` 负责 RabbitMQ 相关能力，`waynboot-data` 封装 Redis / Elasticsearch，`waynboot-util` 放枚举、异常、常量和轻量工具。`waynboot-domain-api` 收敛跨领域契约，`waynboot-domain-trade`、`waynboot-domain-inventory`、`waynboot-domain-goods`、`waynboot-domain-cart`、`waynboot-domain-promotion` 分别承载订单、库存、商品、购物车和营销实现，`waynboot-payment-channel` 承载微信、支付宝、易支付的支付 / 退款渠道适配。`waynboot-common` 只保留通用配置、切面、策略接口、通用模型和基础设施。部署和中间件脚本在 `db-init`、`mysql`、`redis`、`rabbitmq`、`es`、`nginx`、`docker-compose*.yml`。

## 构建、测试与本地运行
在仓库根目录执行：

```bash
mvn clean package
mvn test
mvn -pl waynboot-admin-api spring-boot:run
mvn -pl waynboot-mobile-api spring-boot:run
mvn -pl waynboot-message/waynboot-message-consumer spring-boot:run
```

`mvn clean package` 用于全量构建，`mvn test` 用于执行现有测试。开发时优先按模块启动单个服务，并配合 `docker-compose.yml` 启动 MySQL、Redis、RabbitMQ、Elasticsearch 等依赖。

## Java 开发规范
统一使用 4 空格缩进，类名使用 `UpperCamelCase`，方法和字段使用 `lowerCamelCase`，包名统一小写。启动类命名为 `*Application`。所有源码、XML、Markdown 文件统一使用 UTF-8，避免 BOM 和乱码问题。Controller 只做参数校验、权限判断、日志记录和返回值封装，不写核心业务逻辑；业务逻辑放 Service；MQ Consumer 只负责接收消息和调用 Service。DTO、Request、Response、VO 分层明确，不要直接把数据库实体暴露为接口出参。接口返回参数优先使用明确的 `VO` / `ResVO`，不要继续用 `Map<String, Object>`、裸 `Object` 或临时拼装结构承载复杂出参。禁止硬编码状态值、渠道值、业务常量，统一收敛到常量或枚举。

Controller 层新增或调整接口时，默认补充关键业务日志，至少覆盖入口请求、关键分支和异常上下文，但不要打印密码、token、手机号验证码等敏感信息。对于 `src/main/java/com/wayn/mobile/api/controller` 和 `src/main/java/com/wayn/admin/api/controller` 下的接口，优先保持“入参清晰、返回 VO 化、异常可追踪”的风格。

重构模块代码时，优先目标是高可读性、方便维护，同时兼顾高并发场景下的一致性和后续扩展性。默认保持对外接口契约稳定，内部优先按 facade + support / assembler / helper 的方式拆分，不要继续堆大而全的实现类。

涉及交易、并发、补偿、幂等、缓存一致性等复杂逻辑时，必须补齐注释。至少包含：
- 类级注释：说明该类的职责边界，以及它和编排层、领域层、适配层之间的关系。
- 方法注释：说明输入输出、核心副作用、事务边界或幂等语义。
- 关键分支注释：只写在锁控制、条件更新、库存回滚、优惠券回退、支付回调防重等不易直接看懂的位置，解释“为什么这样做”，不要写机械式注释。

新增的 support / helper / assembler 类必须写类级注释；如果实现类已经退化为单纯委托层，也要用简短注释说明它保留的对外职责，避免后续维护者误判为冗余代码。所有新增方法和本轮修改过的方法，默认补齐方法注释；公共方法必须写，关键私有方法在职责不够直观时也要补齐。

## 模块协作规范
`admin-api`、`mobile-api` 属于入口层，不重复实现领域逻辑。`waynboot-data` 作为中间件访问层，避免在 API 模块中直接散落 Redis / ES 客户端代码。`waynboot-message-consumer` 只做消费编排，不承载复杂业务。`waynboot-util` 保持轻量，不引入强业务依赖。新增公共能力前先确认是否确实被多个入口模块复用，避免继续堆入 `waynboot-common`；领域实现优先落到对应 `waynboot-domain-*` 模块，跨域接口和实体才放入 `waynboot-domain-api`。

商品、订单、支付、优惠券、购物车这类核心链路的改造，优先把查询、校验、状态流转、聚合写入、外部适配解耦，避免继续把并发控制、库存回补、支付回调、ES 同步、优惠券回退等逻辑混在单个 ServiceImpl 中。

## 测试、提交与配置要求
当前自动化测试较少，新增功能至少补充对应模块的服务层或接口层测试，测试类命名为 `*Test.java`。建议逐步迁移到标准目录 `src/test/java`。提交信息沿用当前仓库风格，使用简短中文动宾短句，例如 `修复用户密码更新问题`、`优化订单支付回调逻辑`。

纯单元测试如果直接覆盖 `MyBatis-Plus` 的 `Wrappers.lambdaQuery(...)`、`Wrappers.lambdaUpdate(...)`、`lambdaQuery()`、`lambdaUpdate()` 等调用，先初始化对应实体的 `TableInfo` 缓存，再执行断言；否则容易先被 `lambda cache` 异常打断，导致测试无法真实覆盖业务分支。

涉及 `application*.yml`、数据库、中间件配置的改动，PR 中必须写明影响范围、依赖服务和验证方式，禁止提交真实密钥、账号和本机路径。除非用户明确要求，否则不要提交 `application.yml`、`application-dev.yml` 等本地环境配置文件。

对于商品域、订单域这类阶段性重构，除了代码和测试，还应同步补充面向维护者的说明文档，文档内容至少覆盖背景、范围、核心逻辑调整、并发与一致性变化、验证方式、非目标和后续计划。文档统一放在仓库根目录或 `docs/` 下，命名遵循 `<域>_<阶段>_<主题>.md`。

交易链路优化必须从完整链路评估，不要只做单点代码改动。涉及下单、库存、支付、订单状态机、MQ、本地消息、对账和高可用时，需要先梳理"同步主链路 + 异步补偿链路"两段，并说明幂等键、事务边界、失败补偿方式和运维检查点。当前项目只保留支付流水、渠道账单、退款流水和对账能力，禁止新增 `shop_payment_profit_sharing_flow` 或其他资金拆分流水表及相关实现。

库存相关改造默认遵循“Redis 削峰、MySQL 兜底”的原则。Redis Lua 预扣只能降低入口流量和热点压力，不能替代 MySQL 条件更新、冻结库存和库存流水。支付成功、超时取消、退款回补等库存动作必须能通过库存流水追踪和幂等处理。
