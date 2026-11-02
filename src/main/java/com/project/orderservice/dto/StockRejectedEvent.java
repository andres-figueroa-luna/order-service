package com.project.orderservice.dto;

public record StockRejectedEvent(Long orderId, String productId, String reason) {
}
