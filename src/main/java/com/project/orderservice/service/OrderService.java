package com.project.orderservice.service;

import com.project.orderservice.dto.OrderCreatedEvent;
import com.project.orderservice.dto.OrderRequest;
import com.project.orderservice.model.Order;
import com.project.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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
        order.setStatus("Created");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
          savedOrder.getId(),
          savedOrder.getProductId(),
          savedOrder.getQuantity()
        );

        streamBridge.send("orderCreated-out-0", event);
        log.info("Order {} created and event sent to Kafka", savedOrder.getId());

        return savedOrder;
    }
}
