package com.project.orderservice.service;

import com.project.orderservice.dto.OrderCancelledEvent;
import com.project.orderservice.dto.OrderCreatedEvent;
import com.project.orderservice.dto.OrderRequest;
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
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
          savedOrder.getId(),
          savedOrder.getProductId(),
          savedOrder.getQuantity()
        );

        streamBridge.send("orderCreated-out-0", event);
        log.info("Order {} created, event sent to Kafka", savedOrder.getId());

        return savedOrder;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Order not found with id " + id));
    }

    public Page<Order> getAllOrders(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());

                return orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status provided: " + status);
            }
        }

        return orderRepository.findAll(pageable);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REJECTED) {
            throw new RuntimeException("Cannot cancel order with status " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLING);
        orderRepository.save(order);

        OrderCancelledEvent event = new OrderCancelledEvent(order.getId(), order.getProductId(), order.getQuantity());

        streamBridge.send("orderCancelled-out-0", event);
        log.info("Order {} is being cancelled, event sent to Kafka", order.getId());
    }
}
