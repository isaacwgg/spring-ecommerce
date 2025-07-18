package com.commerce.product.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 500, message = "Product name must be between 3 and 500 characters")
  private String name;
  
  @Positive(message = "Price must be positive")
  private BigDecimal price;
  
  @PositiveOrZero(message = "Stock cannot be negative")
  private int stock;
}