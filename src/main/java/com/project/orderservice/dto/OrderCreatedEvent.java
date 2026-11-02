package com.project.orderservice.dto;

public record OrderCreatedEvent(Long orderId, String productId, Integer quantity) {
}
