package com.project.orderservice.dto;

public record StockRejectedEvent(Long orderId, String sku, String reason) {
}
