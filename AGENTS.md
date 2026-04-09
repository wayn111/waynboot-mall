# Repository Guidelines

## 项目结构与模块划分
本仓库是基于 Spring Boot 3、Java 17 的 Maven 多模块商城后端。业务核心集中在 `waynboot-common`，其中包含 `core`、`dto`、`request`、`response`、`config`、`design` 等共享代码。`waynboot-admin-api` 提供后台管理接口，`waynboot-mobile-api` 提供 H5/移动端接口，`waynboot-message` 负责 RabbitMQ 相关能力，`waynboot-data` 封装 Redis / Elasticsearch，`waynboot-job` 负责 XXL-Job，`waynboot-monitor` 负责监控，`waynboot-util` 放通用工具。部署和中间件脚本在 `db-init`、`mysql`、`redis`、`rabbitmq`、`es`、`nginx`、`docker-compose*.yml`。

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
统一使用 4 空格缩进，类名使用 `UpperCamelCase`，方法和字段使用 `lowerCamelCase`，包名统一小写。启动类命名为 `*Application`。Controller 只做参数校验、权限判断和返回值封装，不写核心业务逻辑；业务逻辑放 Service；MQ Consumer 只负责接收消息和调用 Service。DTO、Request、Response 分层明确，不要直接把数据库实体暴露为接口出参。禁止硬编码状态值、渠道值、业务常量，统一收敛到常量或枚举。

## 模块协作规范
`admin-api`、`mobile-api` 属于入口层，不重复实现领域逻辑。`waynboot-data` 作为中间件访问层，避免在 API 模块中直接散落 Redis / ES 客户端代码。`waynboot-message-consumer` 只做消费编排，不承载复杂业务。`waynboot-util` 保持轻量，不引入强业务依赖。新增公共能力前先确认是否确实被多个入口模块复用，避免继续堆入 `waynboot-common`。

## 测试、提交与配置要求
当前自动化测试较少，新增功能至少补充对应模块的服务层或接口层测试，测试类命名为 `*Test.java`。建议逐步迁移到标准目录 `src/test/java`。提交信息沿用当前仓库风格，使用简短中文动宾短句，例如 `修复用户密码更新问题`、`优化订单支付回调逻辑`。涉及 `application*.yml`、数据库、中间件配置的改动，PR 中必须写明影响范围、依赖服务和验证方式，禁止提交真实密钥、账号和本机路径。
