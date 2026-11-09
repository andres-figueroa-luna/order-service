package com.project.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after an order action (e.g., cancellation)",
        example = "{\"id\": 1, \"status\": \"CANCELLING\", \"message\": \"Order cancellation initiated\"}")
public record OrderActionResponse(Long id, String status, String message) {
}
