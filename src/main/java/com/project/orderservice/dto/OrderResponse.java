package com.project.orderservice.dto;

import com.project.orderservice.model.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String sku,
        Integer quantity,
        OrderStatus orderStatus,
        LocalDateTime createdAt) {
}
