package com.commerce.orders.models;

import com.commerce.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
@ToString(exclude = "orderDAO")
public class OrderItemDAO extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderDAO orderDAO;
  private Long productId;
  private int quantity;
}