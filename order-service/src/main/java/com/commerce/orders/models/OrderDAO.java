package com.commerce.orders.models;

import com.commerce.common.model.BaseEntity;
import com.commerce.orders.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;


@Table(name = "orders")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "orderItems")
@Entity
public class OrderDAO extends BaseEntity {
  private Long customerId;
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private OrderStatus status = OrderStatus.CREATED;
  
  @OneToMany(mappedBy = "orderDAO", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrderItemDAO> orderItems = new ArrayList<>();
  
  // Helper method to add items
  public void addOrderItem(OrderItemDAO item) {
    orderItems.add(item);
    item.setOrderDAO(this);
  }
  
}