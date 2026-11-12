# 📦 Order Service

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.8-green)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.0-blue)
![Kafka](https://img.shields.io/badge/Apache_Kafka-3.9.1-black)

> 📌 **Part of the [Event-Driven E-Commerce Architecture](https://github.com/andres-figueroa-luna/event-driven-microservices-portfolio).**
> Clone the umbrella repository to run the complete ecosystem with Docker Compose, PostgreSQL, and Kafka.

## 🎯 Overview
The **Order Service** is the entry point for the SAGA pattern. It exposes a REST API to manage orders, persists data in PostgreSQL, and publishes events to Kafka. It also acts as a consumer, listening for compensations (like stock rejections or restorations) to update the order status.

## 🔌 API Endpoints

### Create Order
`POST /api/orders`
```json
{
    "sku": "LAPTOP-ROG-001",
    "quantity": 2
}
```
*(Publishes `OrderCreatedEvent`)*

### Get Order by ID
`GET /api/orders/{id}`

### List Orders (Paginated)
`GET /api/orders?page=0&size=10&status=CONFIRMED`

### Cancel Order
`PATCH /api/orders/{id}/cancel`
*(Initiates the compensation flow, publishing `OrderCancelledEvent`)*

## 🏗️ Architecture & Tech Stack
*   **Spring Boot 3.5.8 (LTS)** & **Java 21**
*   **Spring Cloud Stream** (Kafka Binder)
*   **Spring Data JPA** (PostgreSQL)
*   **Resilience4j** (Circuit Breaker, Retry, TimeLimiter for Kafka publishing)
*   **Swagger / OpenAPI** for REST documentation.

## 🗄️ Database Schema
Hibernate automatically manages the following tables (`ddl-auto=update`):
*   `orders`: Stores order details and state (`CREATED`, `CONFIRMED`, `REJECTED`, `CANCELLING`, `CANCELLED`).
*   `processed_events`: Used for Idempotency to prevent duplicate Kafka message processing.

## 🚀 Running Locally
You can run this service standalone for local development:
```bash
mvn spring-boot:run
```
*Note: Requires a local PostgreSQL instance on port `5433` and Kafka on port `9092`.*

## 🧪 Testing
The project uses **Testcontainers** to run integration tests against real PostgreSQL and Kafka instances.
```bash
mvn clean verify
```