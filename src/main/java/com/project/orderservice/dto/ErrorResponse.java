package com.project.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response", example = "{\"message\": \"Error details\"}")
public record ErrorResponse(String message) {
}
