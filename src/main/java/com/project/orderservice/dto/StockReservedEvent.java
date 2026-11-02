package com.project.orderservice.dto;

public record StockReservedEvent(Long orderId, String productId, Integer quantity) {
}
