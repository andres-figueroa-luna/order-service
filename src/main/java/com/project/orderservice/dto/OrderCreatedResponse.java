package com.project.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful order creation response",
        example = "{\"id\": 1, \"sku\": \"LAPTOP-ROG-001\", \"message\": \"Order created successfully\"}")
public record OrderCreatedResponse(Long id, String sku, String message) {
}
