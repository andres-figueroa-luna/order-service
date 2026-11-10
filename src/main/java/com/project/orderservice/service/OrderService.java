package com.project.orderservice.service;

import com.project.orderservice.dto.*;
import com.project.orderservice.exception.BusinessRuleException;
import com.project.orderservice.exception.ResourceNotFoundException;
import com.project.orderservice.model.Order;
import com.project.orderservice.model.OrderStatus;
import com.project.orderservice.publisher.KafkaEventPublisher;
import com.project.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, KafkaEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }


    public OrderCreatedResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setSku(request.sku());
        order.setQuantity(request.quantity());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getSku(),
                savedOrder.getQuantity()
        );

        try {
            eventPublisher.publishEvent("orderCreated-out-0", event).join();
            log.info("Order {} created, event sent to Kafka", savedOrder.getId());
        } catch (BusinessRuleException e) {
            log.error("Failed to send event. Rolling back order creation manually.");
            orderRepository.delete(savedOrder);

            throw e;
        }

        return new OrderCreatedResponse(savedOrder.getId(), savedOrder.getSku(),
                "Order created successfully");
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Order with id " + id + " not found"));

        return new OrderResponse(
                order.getId(),
                order.getSku(),
                order.getQuantity(),
                order.getStatus(),
                order.getCreatedAt());
    }

    public PageResponse<OrderResponse> getAllOrders(String status, Pageable pageable) {
        Page<Order> page;

        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                page = orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid status provided: " + status);
            }
        } else {
            page = orderRepository.findAll(pageable);
        }

        List<OrderResponse> content = page.getContent().stream()
                .map(o -> new OrderResponse(
                        o.getId(),
                        o.getSku(),
                        o.getQuantity(),
                        o.getStatus(),
                        o.getCreatedAt()))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    public OrderActionResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REJECTED) {
            throw new BusinessRuleException("Cannot cancel order with status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLING);

        Order savedOrder = orderRepository.save(order);

        OrderCancelledEvent event = new OrderCancelledEvent(order.getId(), order.getSku(), order.getQuantity());

        try {
            eventPublisher.publishEvent("orderCancelled-out-0", event).join();
            log.info("Order {} is being cancelled, event sent to Kafka", savedOrder.getId());
        } catch (BusinessRuleException e) {
            log.error("Failed to send cancel event. Rolling back status to {}", previousStatus);
            savedOrder.setStatus(previousStatus);
            orderRepository.save(savedOrder);

            throw e;
        }

        return new OrderActionResponse(
                order.getId(),
                order.getStatus().name(),
                "Order cancellation initiated");
    }
}
