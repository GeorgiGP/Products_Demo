package com.xyz.products.dto;

import com.xyz.products.common.Product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String category,
        int quantity,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCategory(),
                p.getQuantity(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
