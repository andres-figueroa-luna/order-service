package com.project.orderservice.function;

import com.project.orderservice.dto.StockRejectedEvent;
import com.project.orderservice.dto.StockReservedEvent;
import com.project.orderservice.dto.StockRestoredEvent;
import com.project.orderservice.model.OrderStatus;
import com.project.orderservice.model.ProcessedEvent;
import com.project.orderservice.repository.OrderRepository;
import com.project.orderservice.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class OrderResponseConsumer {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    public OrderResponseConsumer(OrderRepository orderRepository,
                                 ProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Bean
    public Consumer<StockReservedEvent> handleStockReservedEvent() {
        return event -> {
            String eventId = OrderStatus.CONFIRMED.name() + "-" + event.orderId();

            if (!isDuplicatedEvent(eventId)) {
                orderRepository.findById(event.orderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);

                    log.info("Order {} confirmed", event.orderId());
                });
            }
        };
    }

    @Bean
    public Consumer<StockRejectedEvent> handleStockRejectedEvent() {
        return event -> {
            String eventId = OrderStatus.REJECTED.name() + "-" + event.orderId();
            if (!isDuplicatedEvent(eventId)) {
                orderRepository.findById(event.orderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.REJECTED);
                    orderRepository.save(order);
                    log.info("😢 Order {} Rejected: {}", order.getId(), event.reason());
                });
                processedEventRepository.save(new ProcessedEvent(eventId, LocalDateTime.now()));
            }
        };
    }

    @Bean
    public Consumer<StockRestoredEvent> handleStockRestoredEvent() {
        return event -> {
            String eventId = OrderStatus.CANCELLED.name() + "-" + event.orderId();

            if (!isDuplicatedEvent(eventId)) {
                orderRepository.findById(event.orderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED);

                    orderRepository.save(order);
                    log.info("Order {} fully CANCELLED (Stock restored)", order.getId());
                });

                processedEventRepository.save(new ProcessedEvent(eventId, LocalDateTime.now()));
            }
        };
    }

    private boolean isDuplicatedEvent(String eventId) {
        if (processedEventRepository.existsById(eventId)) {
            log.warn("⚠️ {} is already processed", eventId);
            return true;
        }
        return false;
    }
}
