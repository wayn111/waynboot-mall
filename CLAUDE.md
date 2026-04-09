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

## Important Notes

- This project uses Spring Boot 3.x which requires JDK 17 minimum
- For Spring Boot 2.7 / JDK 8 compatibility, use the `springboot-2.7` branch
- RabbitMQ requires manual acknowledgment mode (`acknowledge-mode: manual`)
- Elasticsearch configuration includes shard/replica settings for production deployment
- The project includes monitoring via Spring Boot Admin (port 89)
