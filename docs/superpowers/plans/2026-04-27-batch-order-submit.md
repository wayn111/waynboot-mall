# Batch Order Submit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduce batch order consumption and batch callback submission while preserving per-order idempotency, locking, inventory consistency, and rollback isolation.

**Architecture:** RabbitMQ order consumers receive batches and call a new mobile batch callback endpoint. The mobile callback delegates to `IMobileOrderService.submitBatch`, and `OrderSubmitSupport.submitBatch` loops through existing per-order `submit` logic so each order keeps its own lock, transaction, stock reduction, coupon update, cart cleanup, and delay-message delivery. Failed orders are reported per item; MQ ack/nack uses the batch result to avoid acknowledging partially failed batches.

**Tech Stack:** Java 17, Spring Boot 3.1.4, Spring AMQP manual ack, Fastjson, JUnit 5, Mockito.

---

### Task 1: Add Batch Submit Service Contract

**Files:**
- Modify: `waynboot-common/src/main/java/com/wayn/common/core/service/shop/IMobileOrderService.java`
- Modify: `waynboot-common/src/main/java/com/wayn/common/core/service/shop/impl/MobileOrderServiceImpl.java`
- Modify: `waynboot-common/src/main/java/com/wayn/common/core/service/shop/support/order/OrderSubmitSupport.java`
- Test: `waynboot-common/src/test/java/com/wayn/common/core/service/shop/support/order/OrderSubmitSupportTest.java`

- [ ] Write a failing test proving `submitBatch` invokes existing submit flow once per order and returns failed order numbers instead of hiding failures.
- [ ] Add `submitBatch(List<OrderDTO>)` to `IMobileOrderService`.
- [ ] Implement `MobileOrderServiceImpl.submitBatch` as a facade delegation.
- [ ] Implement `OrderSubmitSupport.submitBatch` with per-order isolation by calling `submit(orderDTO)` inside a loop.
- [ ] Run `mvn -q -o -nsu -pl waynboot-common -am -Dtest=OrderSubmitSupportTest test`.

### Task 2: Add Batch Callback Endpoint

**Files:**
- Modify: `waynboot-mobile-api/src/main/java/com/wayn/mobile/api/controller/callback/SubmitOrderController.java`

- [ ] Add a `POST callback/order/submit/batch` endpoint accepting an `orders` form field containing a JSON array of `OrderDTO`.
- [ ] Cache `success` for every successful order and cache the failure message for every failed order.
- [ ] Return `R.success()` only when all orders succeed; otherwise return `R.error()` so the consumer can nack/retry the batch.
- [ ] Keep the existing single-order endpoint unchanged for compatibility.

### Task 3: Add Batch Mobile Client

**Files:**
- Modify: `waynboot-message/waynboot-message-consumer/pom.xml`
- Modify: `waynboot-message/waynboot-message-consumer/src/main/java/com/wayn/message/consumer/client/mobile/MobileApi.java`
- Modify: `waynboot-message/waynboot-message-consumer/src/main/java/com/wayn/message/consumer/client/mobile/MobileApiImpl.java`

- [ ] Add test dependency for `spring-boot-starter-test`.
- [ ] Add `submitOrders(List<String> bodies)` to `MobileApi`.
- [ ] Parse existing single-message payloads, collect their `order` objects, and call `/callback/order/submit/batch`.
- [ ] Fall back to single-message validation rules for missing `notifyUrl` or non-success response.

### Task 4: Add Batch Order Consumer

**Files:**
- Create: `waynboot-message/waynboot-message-consumer/src/main/java/com/wayn/message/consumer/config/OrderBatchRabbitConfig.java`
- Create: `waynboot-message/waynboot-message-consumer/src/main/java/com/wayn/message/consumer/order/OrderMessageIdSupport.java`
- Modify: `waynboot-message/waynboot-message-consumer/src/main/java/com/wayn/message/consumer/order/OrderPayConsumer.java`
- Test: `waynboot-message/waynboot-message-consumer/src/test/java/com/wayn/message/consumer/order/OrderPayConsumerTest.java`

- [ ] Add an order-specific batch listener factory in Java config, not `application.yml`.
- [ ] Keep manual ack, bounded batch size, bounded receive timeout, and configurable concurrency defaults.
- [ ] Extract message-id resolution so tests can cover missing `spring_returned_message_correlation`.
- [ ] Change `OrderPayConsumer` to receive `List<Message>` and call `mobileApi.submitOrders`.
- [ ] Ack processed duplicate messages immediately and nack the current batch when any unprocessed message fails.
- [ ] Run `mvn -q -o -nsu -pl waynboot-message/waynboot-message-consumer -am -Dtest=OrderPayConsumerTest test`.

### Task 5: Verify Full Build

**Files:**
- No additional files.

- [ ] Run `mvn -q -o -nsu -pl waynboot-common,waynboot-mobile-api,waynboot-message/waynboot-message-consumer -am test`.
- [ ] Run `git diff --check`.
- [ ] Confirm `application*.yml` is unchanged.
