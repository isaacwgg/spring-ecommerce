package com.commerce.orders.dtos;

import com.commerce.common.dto.BaseEntityResponseDTO;
import com.commerce.orders.enums.OrderStatus;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO extends BaseEntityResponseDTO {
  private Long customerId;
  private OrderStatus status;
  private List<OrderItemResponseDTO> orderItems;
}