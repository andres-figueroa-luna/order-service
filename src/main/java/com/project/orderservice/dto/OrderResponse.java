package com.project.orderservice.dto;

import com.project.orderservice.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Order details response",
        example = "{\"id\": 1, \"sku\": \"LAPTOP-ROG-001\", \"quantity\": 2, \"status\": \"CONFIRMED\", \"createdAt\": \"2026-11-05T10:00:00\"}")
public record OrderResponse(
        Long id,
        String sku,
        Integer quantity,
        OrderStatus orderStatus,
        LocalDateTime createdAt) {
}
