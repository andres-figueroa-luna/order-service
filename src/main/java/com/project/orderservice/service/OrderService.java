package com.project.orderservice.service;

import com.project.orderservice.dto.*;
import com.project.orderservice.exception.BusinessRuleException;
import com.project.orderservice.exception.ResourceNotFoundException;
import com.project.orderservice.model.Order;
import com.project.orderservice.model.OrderStatus;
import com.project.orderservice.repository.OrderRepository;
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
    private final StreamBridge streamBridge;

    public OrderService(OrderRepository orderRepository, StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.streamBridge = streamBridge;
    }

    @Transactional
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

        streamBridge.send("orderCreated-out-0", event);
        log.info("Order {} created, event sent to Kafka", savedOrder.getId());

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

    @Transactional
    public OrderActionResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REJECTED) {
            throw new BusinessRuleException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLING);
        orderRepository.save(order);

        OrderCancelledEvent event = new OrderCancelledEvent(order.getId(), order.getSku(), order.getQuantity());

        streamBridge.send("orderCancelled-out-0", event);
        log.info("Order {} is being cancelled, event sent to Kafka", order.getId());

        return new OrderActionResponse(
                order.getId(),
                order.getStatus().name(),
                "Order cancellation initiated");
    }
}
