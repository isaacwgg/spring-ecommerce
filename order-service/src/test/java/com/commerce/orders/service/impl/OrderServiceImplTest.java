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
import com.commerce.orders.serviceinvocation.ProductClient;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private final Long userId = 1L;
    private final String bearerToken = "Bearer token";
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemsRepository orderItemsRepository;
    @Mock
    private ProductClient productClient;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private AuthenticationUtils authUtils;
    @InjectMocks
    private OrderServiceImpl orderService;
    private OrderDAO testOrder;
    private OrderItemDAO testOrderItem;
    private OrderItemRequestDTO orderItemRequestDTO;

    @BeforeEach
    void setUp() {
        testOrder = new OrderDAO();
        testOrder.setId(1L);
        testOrder.setCustomerId(userId);
        testOrder.setStatus(OrderStatus.CREATED);

        testOrderItem = new OrderItemDAO();
        testOrderItem.setId(1L);
        testOrderItem.setProductId(101L);
        testOrderItem.setQuantity(2);
        testOrderItem.setOrderDAO(testOrder);

        testOrder.setOrderItems(new ArrayList<>(List.of(testOrderItem)));

        orderItemRequestDTO = new OrderItemRequestDTO(101L, 3);

        when(authUtils.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    void addToCart_NewCart() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(OrderDAO.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<Void> response = orderService.addToCart(orderItemRequestDTO, bearerToken);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(orderRepository).save(any(OrderDAO.class));
    }

    @Test
    void addToCart_ExistingCart_NewProduct() {
        // Arrange
        OrderItemRequestDTO newItemRequest = new OrderItemRequestDTO(102L, 1);
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderDAO.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<Void> response = orderService.addToCart(newItemRequest, bearerToken);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, testOrder.getOrderItems().size());
    }

    @Test
    void addToCart_ExistingCart_ExistingProduct() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderDAO.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<Void> response = orderService.addToCart(orderItemRequestDTO, bearerToken);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, testOrder.getOrderItems().size());
        assertEquals(3, testOrder.getOrderItems().get(0).getQuantity());
    }

    @Test
    void checkout_Successful() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        ProductResponseDTO productResponse = ProductResponseDTO.builder()
                .id(101L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stock(10)
                .build();
        when(productClient.getByProductIds(any(IdsDTO.class), eq(bearerToken)))
                .thenReturn(List.of(productResponse));
        when(orderRepository.save(any(OrderDAO.class))).thenReturn(testOrder);

        // Act
        OrderResponseDTO result = orderService.checkout(1L, bearerToken);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(productClient).batchDecreaseStock(any(StockUpdateBatchDTO.class), eq(bearerToken));
    }

    @Test
    void checkout_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.checkout(99L, bearerToken));
    }

    @Test
    void checkout_InvalidStatus() {
        // Arrange
        testOrder.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.checkout(1L, bearerToken));
    }

    @Test
    void checkout_InsufficientStock() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        ProductResponseDTO productResponse = ProductResponseDTO.builder()
                .id(101L)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stock(1)
                .build();
        when(productClient.getByProductIds(any(IdsDTO.class), eq(bearerToken)))
                .thenReturn(List.of(productResponse));

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.checkout(1L, bearerToken));
    }

    @Test
    void clearCart_Successful() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.of(testOrder));

        // Act
        ResponseEntity<Void> response = orderService.clearCart();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(orderItemsRepository).deleteAll(testOrder.getOrderItems());
        verify(orderRepository).delete(testOrder);
    }

    @Test
    void clearCart_NoCartFound() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.clearCart());
    }


    @Test
    void getOrders_Successful() {
        // Arrange
        OrderResponseDTO mockResponse = OrderResponseDTO.builder()
                .customerId(1L)
                .customerId(userId)
                .status(OrderStatus.CREATED)
                .orderItems(List.of())  // Assuming this is List<OrderItemResponseDTO>
                .build();

        when(orderRepository.findAll()).thenReturn(List.of(testOrder));
        when(modelMapper.map(testOrder, OrderResponseDTO.class))
                .thenReturn(mockResponse);

        // Act
        List<OrderResponseDTO> result = orderService.getOrders();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(mockResponse, result.get(0));
    }

    @Test
    void findById_Successful() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDAO result = orderService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.findById(99L));
    }

    @Test
    void getCurrentCart_Successful() {
        // Arrange
        when(orderRepository.findByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(List.of(testOrder));

        // Act
        List<OrderDAO> result = orderService.getCurrentCart(userId);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void removeProductFromCart_Successful() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.of(testOrder));
        when(orderItemsRepository.findFirstByOrderDAOAndProductId(testOrder, 101L))
                .thenReturn(Optional.of(testOrderItem));

        // Act
        ResponseEntity<Void> response = orderService.removeProductFromCart(101L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(orderItemsRepository).delete(testOrderItem);
    }

    @Test
    void removeProductFromCart_EmptyCart() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.removeProductFromCart(101L));
    }

    @Test
    void removeProductFromCart_ProductNotFound() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.of(testOrder));
        when(orderItemsRepository.findFirstByOrderDAOAndProductId(testOrder, 102L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> orderService.removeProductFromCart(102L));
    }

    @Test
    void removeProductFromCart_DeletesOrderWhenEmpty() {
        // Arrange
        when(orderRepository.findFirstByCustomerIdAndStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.of(testOrder));
        when(orderItemsRepository.findFirstByOrderDAOAndProductId(testOrder, 101L))
                .thenReturn(Optional.of(testOrderItem));
        when(orderItemsRepository.findByOrderDAO(testOrder)).thenReturn(List.of());

        // Act
        ResponseEntity<Void> response = orderService.removeProductFromCart(101L);

        // Assert
        assertNotNull(response);
        verify(orderRepository).delete(testOrder);
    }
}