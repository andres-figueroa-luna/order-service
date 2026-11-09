package com.project.orderservice.dto;

public record OrderCancelledEvent(Long orderId, String sku, Integer quantity) {
}
