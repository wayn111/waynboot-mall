# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

waynboot-mall is a complete H5 e-commerce system with three main components:
- Admin backend API (waynboot-admin-api)
- Mobile H5 mall API (waynboot-mobile-api)  
- Message consumer for async processing (waynboot-message-consumer)

Tech stack: Spring Boot 3.1.4, JDK 17, MyBatis Plus, Spring Security, Redis, RabbitMQ, Elasticsearch 7.x

## Build and Run Commands

### Build the project
```bash
mvn clean install
```

### Run individual services

**Admin API (port 81):**
```bash
cd waynboot-admin-api
mvn spring-boot:run
```
Or run `com.wayn.AdminApplication` directly in IDE

**Mobile API (port 82):**
```bash
cd waynboot-mobile-api
mvn spring-boot:run
```
Or run `com.wayn.MobileApplication` directly in IDE

**Message Consumer:**
```bash
cd waynboot-message/waynboot-message-consumer
mvn spring-boot:run
```
Or run `com.wayn.MessageApplication` directly in IDE

### Run tests
```bash
mvn test
```

## Architecture

### Module Structure

This is a multi-module Maven project with clear separation of concerns:

- **waynboot-common**: Core business logic shared across all services
  - `core.entity`: Domain entities (shop, system, tool packages)
  - `core.mapper`: MyBatis Plus mappers
  - `core.service`: Business service layer
  - Shared configurations, DTOs, requests/responses

- **waynboot-data**: Data access abstraction layer
  - `waynboot-data-redis`: Redis operations and caching
  - `waynboot-data-elastic`: Elasticsearch operations for product search

- **waynboot-message**: Message queue infrastructure
  - `waynboot-message-core`: Queue/exchange definitions and bindings
  - `waynboot-message-consumer`: Message consumers for async tasks

- **waynboot-util**: Base utilities, constants, enums, exceptions

- **waynboot-admin-api**: Admin backend REST API
  - Controllers in `controller` package
  - Security config in `framework.config`

- **waynboot-mobile-api**: H5 mobile mall REST API
  - Controllers in `controller` package
  - Security config in `framework.config`

- **waynboot-monitor**: Spring Boot Admin monitoring
- **waynboot-job**: xxl-job distributed task scheduling

### Key Design Patterns

- **Master-Slave DB**: Configured in `application-dev.yml` (slave disabled by default)
- **Layered Architecture**: Controller → Service → Mapper → Entity
- **Shared Core**: Common business logic in waynboot-common, consumed by API modules
- **Message-Driven**: RabbitMQ for async operations (order processing, notifications)
- **Search Optimization**: Elasticsearch for product search with IK analyzer and pinyin support

## Configuration

### Required Infrastructure

Before running locally, ensure these services are running:
- MySQL 8.0+ (database: `wayn_shop`)
- Redis 3.0+
- RabbitMQ 3.0+ with delayed message plugin
- Elasticsearch 7.x with IK word segmentation and pinyin plugins

### Configuration Files

- `waynboot-common/src/main/resources/application-dev.yml`: Database, Redis, RabbitMQ, ES, payment gateway configs
- `waynboot-admin-api/src/main/resources/application.yml`: Admin API settings (port 81)
- `waynboot-mobile-api/src/main/resources/application.yml`: Mobile API settings (port 82)

### Database Setup

Import SQL files from project root: `wayn_shop_*.sql` into MySQL database named `wayn_shop`

### Image Storage

Product images are stored locally at `D:/waynshop/webp` by default. Configure via `wayn.uploadDir` in application.yml or `UPLOAD_DIR` environment variable.

### Payment Integration

The system supports three payment methods (configured in application-dev.yml):
- WeChat Pay (`shop.wxpay.*`)
- Alipay (`shop.alipay.*`)
- EPay (`shop.epay.*`)

Certificate files for WeChat Pay should be placed in resources folder.

## Development Conventions

### Security

- Admin API uses Spring Security with JWT tokens (header: `Authorization`)
- Token expiry configured via `token.expireTime` (default 120 minutes)
- Mobile API has separate security configuration for H5 users

### MyBatis Plus

- Logical delete enabled globally: `delFlag` field (0=active, 1=deleted)
- Mapper XMLs located in `classpath*:mapper/**/*Mapper.xml`
- Entity aliases package: `com.wayn.**.domain`

### Logging

- Log files written to `${LOG_PATH_PREFIX}/${spring.application.name}/info.log`
- Default log path: `E:/home` (Windows) or `/home` (Linux)
- Log level: INFO for application and Spring framework

### API Documentation

API documentation is maintained in Apifox: https://apifox.com/apidoc/shared-f48b11f5-6137-4722-9c70-b9c5c3e5b09b

## Important Notes

- This project uses Spring Boot 3.x which requires JDK 17 minimum
- For Spring Boot 2.7 / JDK 8 compatibility, use the `springboot-2.7` branch
- RabbitMQ requires manual acknowledgment mode (`acknowledge-mode: manual`)
- Elasticsearch configuration includes shard/replica settings for production deployment
- The project includes monitoring via Spring Boot Admin (port 89)
