package com.commerce.orders.service.impl;

import com.commerce.common.config.utils.AuthenticationUtils;
import com.commerce.common.dto.IdsDTO;
import com.commerce.common.dto.ProductResponseDTO;
import com.commerce.common.dto.StockUpdateBatchDTO;
import com.commerce.orders.dtos.OrderItemRequestDTO;
import com.commerce.orders.dtos.OrderResponseDTO;
import com.commerce.orders.enums.OrderStatus;
import com.commerce.orders.models.OrderDAO;
import com.commerce.orders.models.OrderItemDAO;
import com.commerce.orders.repository.OrderItemsRepository;
import com.commerce.orders.repository.OrderRepository;
import com.commerce.orders.service.OrderService;
import com.commerce.orders.serviceinvocation.ProductClient;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductClient productClient;
    private final ModelMapper modelMapper;
    private final AuthenticationUtils authUtils;

    public OrderServiceImpl(OrderRepository r, OrderItemsRepository orderItemsRepository, ProductClient productClient, ModelMapper modelMapper, AuthenticationUtils authUtils) {
        this.orderRepository = r;
        this.orderItemsRepository = orderItemsRepository;
        this.productClient = productClient;
        this.modelMapper = modelMapper;
        this.authUtils = authUtils;
    }

    private void addProductToCart(OrderItemRequestDTO orderItemRequestDTO, OrderDAO cart) {
        // Check if product already exists in cart
        OrderItemDAO existingItem = cart.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(orderItemRequestDTO.productId()))
                .findFirst()
                .orElse(null);
        log.info("Found existing item in cart: {}", existingItem);

        if (existingItem != null) {
            // Update quantity of existing item
            existingItem.setQuantity(orderItemRequestDTO.quantity());
            log.info("Updated quantity for product {} in cart. New quantity: {}",
                    orderItemRequestDTO.productId(), existingItem.getQuantity());
        } else {
            // Add new item to cart
            OrderItemDAO newItem = OrderItemDAO.builder()
                    .orderDAO(cart)
                    .productId(orderItemRequestDTO.productId())
                    .quantity(orderItemRequestDTO.quantity())
                    .build();
            cart.addOrderItem(newItem);
            log.info("Added new product {} to cart with quantity {}",
                    orderItemRequestDTO.productId(), orderItemRequestDTO.quantity());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Void> addToCart(OrderItemRequestDTO orderItemRequestDTO, String bearerToken) {
        Long userId = authUtils.getCurrentUserId();

        // Find existing cart (order with CREATED status) for the user
        OrderDAO cart = orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED).orElse(OrderDAO.builder()
                .customerId(userId)
                .status(OrderStatus.CREATED)
                .build());
        log.info("Found cart for user {}: {}", userId, cart);
        addProductToCart(orderItemRequestDTO, cart);

        orderRepository.save(cart);
        log.info("Cart updated successfully for user {}", userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public OrderResponseDTO checkout(Long orderId, String bearerToken) {
        OrderDAO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ServiceException("Order cannot be checked out. Current status: " + order.getStatus());
        }

        loadAndValidateProductAvailability(bearerToken, order);
        updateStockQuantity(bearerToken, order);
        order.setStatus(OrderStatus.PAID);
        log.info("Order {} checked out successfully with {} items", orderId, order.getOrderItems().size());
        //return orderRepository.save(order);
        OrderDAO savedOrder = orderRepository.save(order);
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomerId());
        response.setStatus(savedOrder.getStatus());
        return response;
    }

    private void loadAndValidateProductAvailability(String bearerToken, OrderDAO order) {
        // Collect all product IDs from the order
        List<Long> productIds = order.getOrderItems().stream()
                .map(OrderItemDAO::getProductId)
                .toList();

        // Make a single batch request to get all products
        IdsDTO idsDTO = IdsDTO.builder()
                .ids(productIds)
                .build();

        // This should return List<ProductResponseDto> based on the controller implementation
        var products = productClient.getByProductIds(idsDTO, bearerToken);

        // Create a map of product ID to Product for easy access
        Map<Long, ProductResponseDTO> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponseDTO::getId, product -> product));
        // Validate stock for all items using the product map
        List<String> validationErrors = new ArrayList<>();

        for (OrderItemDAO item : order.getOrderItems()) {
            ProductResponseDTO product = productMap.get(item.getProductId());
            if (product == null) {
                String errorMsg = "Product not found for ID: " + item.getProductId();
                log.error(errorMsg);
                validationErrors.add(errorMsg);
                continue; // Skip stock validation if product doesn't exist
            }

            if (product.getStock() < item.getQuantity()) {
                String errorMsg = "Insufficient stock for product ID: " + product.getName() +
                        " (requested: " + item.getQuantity() + ", available: " + product.getStock() + ")";
                validationErrors.add(errorMsg);
            }
        }

// Throw all validation errors at once
        if (!validationErrors.isEmpty()) {
            String combinedErrors = String.join("; ", validationErrors);
            log.error(combinedErrors);
            throw new ServiceException("Validation failed: " + combinedErrors);
        }
    }

    private void updateStockQuantity(String bearerToken, OrderDAO order) {
        // Decrease stock for all items in a single batch request
        List<StockUpdateBatchDTO.StockUpdateItem> stockUpdates = order.getOrderItems().stream()
                .map(item -> StockUpdateBatchDTO.StockUpdateItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        StockUpdateBatchDTO batchDTO = StockUpdateBatchDTO.builder()
                .items(stockUpdates)
                .build();

        productClient.batchDecreaseStock(batchDTO, bearerToken);
    }

    @Transactional
    @Override
    public ResponseEntity<Void> clearCart() {
        Long userId = authUtils.getCurrentUserId();

        // Find all orders in CREATED status for the current user
        OrderDAO order = orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED).orElseThrow(() -> new ServiceException("No cart found for user: " + userId));

        // Delete all cart orders and their items
        orderItemsRepository.deleteAll(order.getOrderItems());
        orderRepository.delete(order);

        log.info("Cart cleared successfully for user: {}, removed {} products", userId, order.getOrderItems().size());
        return ResponseEntity.ok().build();
    }

    @Override
    public List<OrderResponseDTO> getOrders() {
        return orderRepository.findAll().stream()
                .map(orderDAO -> {
                    orderDAO.getOrderItems().size();
                    return modelMapper.map(orderDAO, OrderResponseDTO.class);
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderDAO findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Order not found"));
    }

    @Override
    public List<OrderDAO> getCurrentCart(Long customerId) {
        return orderRepository.findByCustomerIdAndStatus(customerId, OrderStatus.CREATED);

    }

    @Transactional
    @Override
    public ResponseEntity<Void> removeProductFromCart(Long productId) {
        Long userId = authUtils.getCurrentUserId();

        // Find all orders in CREATED status for the current user
        OrderDAO order = orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED).orElseThrow(() -> new ServiceException("Cart is empty"));

        // Find order items with the specific product ID
        OrderItemDAO itemToRemove = orderItemsRepository.findFirstByOrderDAOAndProductId(order, productId).orElseThrow(() -> new ServiceException("Product not found in cart"));

        // Remove all items with this product ID from this order
        orderItemsRepository.delete(itemToRemove);

        // Check if the order has any remaining items
        List<OrderItemDAO> remainingItems = orderItemsRepository.findByOrderDAO(order);
        if (remainingItems.isEmpty()) {
            // If no items left, delete the entire order
            orderRepository.delete(order);
            log.info("Order {} deleted as it had no remaining items", order.getId());
        }

        log.info("Product {} removed from cart for user: {}", productId, userId);
        return ResponseEntity.ok().build();
    }
}