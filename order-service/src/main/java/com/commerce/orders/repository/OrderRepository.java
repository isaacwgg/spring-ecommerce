package com.commerce.orders.repository;

import com.commerce.orders.enums.OrderStatus;
import com.commerce.orders.models.OrderDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderDAO, Long> {
  List<OrderDAO> findByCustomerId(Long customerId);
  
  List<OrderDAO> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
  
  Optional<OrderDAO> findFirstByCustomerIdAndStatus(Long customerId, OrderStatus status);
  
}
