package com.project.orderservice.publisher;

import com.project.orderservice.exception.BusinessRuleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaEventPublisher {

    private final StreamBridge streamBridge;

    public KafkaEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "fallbackPublish")
    @Retry(name = "kafkaPublisher")
    @TimeLimiter(name = "kafkaPublisher")
    public CompletableFuture<Void> publishEvent(String bindingName, Object event) {
        return CompletableFuture.runAsync(() -> {
            streamBridge.send(bindingName, event);
            log.info("Event sent to Kafka binding: {}", bindingName);
        });
    }

    public CompletableFuture<Void> fallbackPublish(String bindingName, Object event, Throwable throwable) {
        log.error("Circuit breaker/TimeLimiter triggered for Kafka. Failed to publish event to: {}", bindingName, throwable);
        throw new BusinessRuleException("Messaging system is unavailable. Please try again later.");
    }
}
