# Order Service - Event-Driven Architecture Demo
This project is a robust, production-ready microservice built with Java 21 and Spring Boot 4, designed to demonstrate a Choreography-based SAGA pattern for distributed transactions.

It acts as the first step in an e-commerce workflow: receiving HTTP requests, persisting orders in PostgreSQL, and publishing OrderCreated events to Apache Kafka. It also acts as a consumer, listening for StockReserved or StockRejected events to update the order status accordingly.

Key Technical Features:

    Asynchronous Communication: Powered by Spring Cloud Stream and Apache Kafka (KRaft mode).

    Idempotent Consumers: Prevents duplicate event processing using a dedicated database table.

    Resilience: Configured with Dead Letter Queues (DLQ) to handle poisonous messages.

    Containerized Infrastructure: Fully testable using Testcontainers for real integration testing.
