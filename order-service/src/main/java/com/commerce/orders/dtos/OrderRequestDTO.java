package com.commerce.orders.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;


@Builder
public record OrderRequestDTO(
    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "At least one order item is required")
    List<@Valid OrderItemRequestDTO> items
) {
}