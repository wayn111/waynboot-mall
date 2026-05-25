# waynboot-mall

`waynboot-mall` is a Maven multi-module e-commerce backend based on Spring Boot 3 and Java 17. It covers the mobile storefront API, admin API, message consumers, scheduled jobs, Redis, and Elasticsearch. The current architecture focuses on goods, orders, inventory, payments, MQ, local message compensation, and reconciliation governance, with the goal of keeping the trade flow maintainable, scalable, and reliable under high concurrency.

## Technology Stack

| Category | Technologies |
| --- | --- |
| Base Framework | Spring Boot 3.1.4, Java 17 |
| Web and Security | Spring MVC, Spring Security, JWT |
| Data Access | MyBatis-Plus, MySQL, Druid |
| Cache | Redis, Lettuce, Lua scripts |
| Search | Elasticsearch 7 |
| Messaging | RabbitMQ, delayed messages, local message table |
| Scheduled Jobs | Spring Scheduled |
| Payment | WeChat Pay, Alipay, EPay |
| Testing | JUnit 5, Mockito, Spring Boot Test |

## Module Structure

```text
waynboot-mall
├── waynboot-admin-api              Admin management APIs
├── waynboot-mobile-api             H5 / mobile storefront APIs
├── waynboot-common                 Common configuration, aspects, strategy contracts, models, infrastructure
├── waynboot-domain-api             Cross-domain contracts: entities, mappers, service interfaces, VOs, enums
├── waynboot-domain-trade           Orders, payment orchestration, state machine, local messages, reconciliation
├── waynboot-domain-inventory       Stock freeze, inventory flows, Redis stock snapshots, reconciliation
├── waynboot-domain-goods           Goods, SKUs, categories, search, and Elasticsearch sync
├── waynboot-domain-cart            Cart reads, writes, and checked goods aggregation
├── waynboot-domain-promotion       Coupons, marketing slots, and Diamond strategy implementations
├── waynboot-payment-channel        WeChat Pay, Alipay, and EPay payment / refund adapters
├── waynboot-data
│   ├── waynboot-data-redis         Redis access and cache utilities
│   └── waynboot-data-elastic       Elasticsearch access capabilities
├── waynboot-message
│   ├── waynboot-message-core       RabbitMQ queues, exchanges, and binding configuration
│   └── waynboot-message-consumer   MQ consumers
├── waynboot-util                   Common utilities, exceptions, enums, constants
├── db-init                         Database initialization and incremental governance SQL
├── redis / rabbitmq / es / nginx   Middleware configuration
└── docker-compose*.yml             Local or server orchestration scripts
```

## Architecture Layers

The project is organized around the entry layer, orchestration layer, domain capability layer, asynchronous consistency layer, and governance layer.

```text
Controller entry layer
  -> Parameter validation, authorization, logging, and VO response wrapping

Service / Support orchestration layer
  -> Order submission chain, payment callback, order state transition, compensation entry points

Domain capability layer
  -> Goods, cart, order, inventory, coupon, payment flow, order status log

Asynchronous consistency layer
  -> Local message table, MQ relay, consumer template, compensation retry

Governance layer
  -> Redis traffic shaping, inventory reconciliation, payment reconciliation, compensation admin APIs, Spring Scheduled jobs
```

Entry modules do not own core business logic. `waynboot-admin-api` and `waynboot-mobile-api` mainly adapt HTTP requests and responses. Trade, inventory, goods, cart, and promotion implementations are split into `waynboot-domain-*` modules. `waynboot-domain-api` keeps cross-domain contracts, while `waynboot-common` keeps common configuration, aspects, strategy contracts, shared models, and infrastructure.

## Core Capabilities

### Goods and Cart

- Admin APIs manage goods, SKUs, specifications, attributes, categories, and inventory data.
- Mobile goods detail APIs support aggregate queries and Redis caching.
- Goods detail cache is invalidated after goods writes, inventory changes, or virtual sales updates to avoid stale stock on detail pages.
- Cart aggregation combines goods, SKUs, coupons, and price information, and API responses use explicit VOs instead of exposing database entities.

### Order Flow

The order flow is split by responsibility chain and domain support services. The core steps are:

```text
User submits order
-> Validate request and cart data
-> Redis Lua stock pre-deduction
-> MySQL conditional stock freeze
-> Insert order and order goods records
-> Insert local message records
-> MQ / local processors execute asynchronous post actions
```

Order state transitions are managed by `OrderStateTransitionSupport`. Payment, cancel, delivery, refund, and confirm-receipt entries must validate state transition rules and use database conditional updates on the current state to avoid concurrent overwrites.

### Inventory Consistency

The inventory model mainly uses available stock and locked stock:

```text
number       Available stock
lockedStock  Locked stock
```

Inventory change rules:

```text
Order freeze:       number - n, lockedStock + n
Payment confirm:    lockedStock - n
Timeout cancel:     number + n, lockedStock - n
Refund compensation:number + n
```

Design principles:

- Redis Lua is only an entry traffic-shaping and hot-SKU concurrency gate.
- MySQL conditional updates provide final consistency and prevent overselling.
- Inventory flow records use a unique `flowKey` to keep inventory side effects idempotent during local message retries.
- `InventoryReconciliationService` can derive locked stock from inventory flows and identify or repair `lockedStock` differences.
- Hot SKUs can reduce single-key contention through Redis stock snapshots and bucketed keys.

### Payment and Reconciliation

Payment callbacks support WeChat Pay, Alipay, and EPay. Callback handling follows these rules:

- Business handling starts only after signature verification succeeds.
- Order status updates must include current-state conditions.
- Third-party payment flows are uniquely constrained by `shop_payment_flow.flow_key`.
- Repeated callbacks return idempotent success.
- Post-payment actions, such as stock confirmation and virtual sales update, are processed asynchronously through local messages.

Payment reconciliation covers:

- Payment flow vs order status / order amount.
- Channel bill vs internal payment flow.
- Refund flow vs order refund amount.
- Spring Scheduled jobs provide the daily reconciliation entry point.

### Local Message and Compensation

The local message table solves the problem where the business transaction commits successfully but MQ delivery fails.

```text
Write business data + local_message in the same business transaction
-> Relay scans INIT messages
-> Deliver to RabbitMQ or execute local processors
-> Mark SENT on success
-> Retry failures with exponential backoff
-> Mark FAILED after max retries
-> Manually retry through admin APIs or compensate through scheduled jobs
```

Compensation governance capabilities:

- `LocalMessageService` manages message creation, query, success marking, and failed retry.
- `LocalMessageFailureClassifier` classifies failure reasons.
- `LocalMessageCompensationLogService` records failure, dead-letter, and manual retry logs.
- `TradeOpsController` provides failed message query, metrics, and manual retry APIs.

### Admin Dashboard Statistics

Admin dashboard statistics are provided by `DashboardController` and exposed under `/shop/dashboard`. They cover core metrics, sales trends, payment channels, top-selling goods, low-stock warnings, member trends, and recent activities. Statistical rules are centralized in `DashboardService`, while `waynboot-admin/src/views/dashboard` only handles presentation and no longer keeps hardcoded dashboard data.

See the “后台首页统计逻辑” section in [ONBOARDING.md](./ONBOARDING.md) for detailed statistical rules, API contracts, and frontend integration notes.

## Scheduled Jobs

Scheduled jobs are implemented with Spring `@Scheduled` and run inside the `waynboot-admin-api` process so they share the same dependencies as `TradeOpsController`. The implementation lives in `com.wayn.admin.api.schedule.TradeGovernanceScheduledTask`:

```text
refreshStockSnapshot         Refresh Redis stock snapshots, supports hot-SKU buckets (every 5 minutes by default)
reconcileInventory           Reconcile inventory flows, optionally repairs lockedStock (hourly by default)
reconcilePaymentDaily        Run daily payment reconciliation (02:00 every day by default)
```

Cron expressions, batch limits, and the auto-repair switch can be tuned via `wayn.schedule.trade.*` in `application.yml`:

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

## Database Scripts

Core scripts are located in `db-init`:

```text
wayn_shop_*.sql                  Base mall schema and initialization data
inventory_stock.sql              Locked stock fields and inventory flow table
local_message.sql                Local message table and compensation log table
order_status_log.sql             Order status log table
payment_flow.sql                 Payment flow, channel bill, and refund flow tables
```

SQL files are retained as database evolution entry points. Before applying them, confirm the execution order and idempotency against the target environment.

## Local Development

### Requirements

- JDK 17
- Maven 3.8+
- MySQL 8+
- Redis 6+
- RabbitMQ 3+
- Elasticsearch 7

### Build and Test

```bash
mvn clean package
mvn test
```

To use a local Maven repository directory:

```bash
mvn "-Dmaven.repo.local=.m2/repository" test
```

### Start Services

```bash
mvn -pl waynboot-admin-api spring-boot:run
mvn -pl waynboot-mobile-api spring-boot:run
mvn -pl waynboot-message/waynboot-message-consumer spring-boot:run
```

You can also start each module's `*Application` class from the IDE.

### Middleware

The repository provides Docker Compose orchestration files:

```bash
docker-compose up -d
```

If MySQL, Redis, RabbitMQ, and Elasticsearch need to be started together, choose `docker-compose-es7-rabbitmq-redis-mysql.yml` according to the local environment.

## Development Rules

- Use UTF-8 for all source code, XML, and Markdown files.
- Controllers only handle validation, authorization, logging, and VO response wrapping. They should not contain core business logic.
- API responses should use explicit `VO` / `ResVO` classes instead of raw `Map<String, Object>` for complex structures.
- Trade, inventory, payment, compensation, and idempotency logic must include class comments, method comments, and key branch comments.
- Pure unit tests that directly use MyBatis-Plus Lambda Wrappers must initialize the entity `TableInfo` cache first.
- Do not commit local configuration files such as `application*.yml`, `application*.yaml`, or `application*.properties` by default.

## Recommended Code Entry Points

```text
OrderStockSupport                 Stock freeze, confirm, release, and refund compensation
RedisStockPreDeductSupport         Redis Lua stock pre-deduction
RedisStockSnapshotSupport          Redis stock snapshot warm-up and buckets
OrderStateTransitionSupport        Order state machine
PaymentCallbackSupport             Unified payment callback handling
PaymentReconciliationService       Payment, channel bill, and refund reconciliation
LocalMessageService                Local message state transition
LocalMessageRelaySupport           Local message delivery and local processor dispatching
TradeOpsController                 Admin trade governance APIs
TradeGovernanceScheduledTask       Trade governance Spring Scheduled tasks
DashboardController                Admin dashboard statistics APIs
DashboardService                   Dashboard statistical rules and VO assembly
AdminOrderMapper                   Admin order queries and top-selling goods aggregation
```

Main code locations:

```text
waynboot-domain-trade/src/main/java/com/wayn/domain/trade/support/order
waynboot-domain-trade/src/main/java/com/wayn/domain/trade/support/payment
waynboot-domain-trade/src/main/java/com/wayn/domain/trade/outbox
waynboot-domain-inventory/src/main/java/com/wayn/domain/inventory/support
waynboot-domain-inventory/src/main/java/com/wayn/domain/inventory/service
```

## Frontend Projects

- H5 storefront: `waynboot-mobile`
- Admin frontend: `waynboot-admin`

This backend project only provides APIs and governance capabilities. See the corresponding frontend repositories for frontend startup and deployment instructions.

## License

This project follows the `LICENSE` file in the repository.
