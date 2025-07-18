package com.commerce.orders.dtos;

import com.commerce.common.dto.BaseEntityResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO extends BaseEntityResponseDTO {
  private Long id;
  private Long productId;
  private Integer quantity;
  private BigDecimal price;
  
}
