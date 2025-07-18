package com.commerce.orders.repository;

import com.commerce.orders.models.OrderDAO;
import com.commerce.orders.models.OrderItemDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemsRepository extends JpaRepository<OrderItemDAO, Long> {
  List<OrderItemDAO> findByOrderDAO(OrderDAO orderDAO);
  
  Optional<OrderItemDAO> findFirstByOrderDAOAndProductId(OrderDAO orderDAO, Long productId);
  
}
