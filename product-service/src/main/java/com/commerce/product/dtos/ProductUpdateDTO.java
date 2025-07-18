package com.commerce.product.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateDTO(
    @Positive
    Long id,
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 500, message = "Product name must be between 3 and 500 characters")
    String name,
    
    @Positive(message = "Price must be positive")
    BigDecimal price,
    
    @PositiveOrZero(message = "Stock cannot be negative")
    int stock
) {
}