# Order Service - Event-Driven Architecture 🚀

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-green)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.1.1-blue)
![Kafka](https://img.shields.io/badge/Apache_Kafka-KRaft-black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

This project is a robust, production-ready microservice built with **Java 21 and Spring Boot 4**, designed to demonstrate a **Choreography-based SAGA pattern** for distributed transactions.

It acts as the first step in an e-commerce workflow: receiving HTTP requests, persisting orders in **PostgreSQL**, and publishing `OrderCreated` events to **Apache Kafka**. It also acts as a consumer, listening for `StockReserved` or `StockRejected` events to update the order status accordingly.

## 🏗 Architecture & Flow

The system uses an Event-Driven Architecture. Below is the sequence of a successful order:

```mermaid
sequenceDiagram
    participant Client
    participant OS as Order Service
    participant DB as PostgreSQL
    participant Kafka as Apache Kafka
    participant IS as Inventory Service
    
    Client->>OS: POST /api/orders (JSON)
    OS->>DB: Save Order (Status: CREATED)
    OS->>Kafka: Publish OrderCreatedEvent (orders-topic)
    Kafka->>IS: Consume OrderCreatedEvent
    IS->>IS: Check & Update Stock
    alt Stock Available
        IS->>Kafka: Publish StockReservedEvent (stock-reserved-topic)
        Kafka->>OS: Consume StockReservedEvent
        OS->>DB: Update Order (Status: CONFIRMED)
    else Out of Stock
        IS->>Kafka: Publish StockRejectedEvent (stock-rejected-topic)
        Kafka->>OS: Consume StockRejectedEvent
        OS->>DB: Update Order (Status: REJECTED)
    end
