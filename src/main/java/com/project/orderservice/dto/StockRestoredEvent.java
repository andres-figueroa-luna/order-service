package com.project.orderservice.dto;

public record StockRestoredEvent(Long orderId, String sku, Integer quantity) {
}
