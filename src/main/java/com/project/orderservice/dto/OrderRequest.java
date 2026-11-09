package com.project.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @Schema(description = "The unique SKU of the product to order",
                example = "LAPTOP-ROG-001")
        @NotBlank(message = "SKU is required")
        String sku,

        @Schema(description = "The number of items to order", example = "2", minimum = "1")
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity) {
}
