package com.commerce.orders.service;

import com.commerce.orders.dtos.OrderItemRequestDTO;
import com.commerce.orders.dtos.OrderResponseDTO;
import com.commerce.orders.models.OrderDAO;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {

    @Transactional
    ResponseEntity<Void> addToCart(OrderItemRequestDTO orderItemRequestDTO, String bearerToken);

    OrderResponseDTO checkout(Long orderId, String bearerToken);


    @Transactional
    ResponseEntity<Void> clearCart();

    List<OrderResponseDTO> getOrders();

    OrderDAO findById(Long id);

    List<OrderDAO> getCurrentCart(Long customerId);


    @Transactional
    ResponseEntity<Void> removeProductFromCart(Long productId);
}
