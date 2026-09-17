package com.xyz.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "name must not be blank")
        String name,

        @NotNull(message = "price is required")
        @Positive(message = "price must be greater than zero")
        BigDecimal price,

        @NotBlank(message = "category must not be blank")
        String category,

        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity must not be negative")
        Integer quantity,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description
) {
}
