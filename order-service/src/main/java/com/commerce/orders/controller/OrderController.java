package com.commerce.orders.controller;

import com.commerce.common.constants.OpenAPIConstants;
import com.commerce.orders.dtos.OrderItemRequestDTO;
import com.commerce.orders.dtos.OrderResponseDTO;
import com.commerce.orders.models.OrderDAO;
import com.commerce.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.service.spi.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Controller", description = "APIs for order management")
@SecurityRequirement(name = OpenAPIConstants.BEARER_SECURITY_SCHEME)
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService s) {
        this.orderService = s;
    }

    @Operation(
            summary = "Get all orders",
            description = "Retrieves a list of all orders"
    )
    @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully")
    @GetMapping("")
    public List<OrderResponseDTO> getAll() {
        return orderService.getOrders();
    }

    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the provided details"
    )
    @ApiResponse(responseCode = "200", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid order data")
    @ApiResponse(responseCode = "403", description = "Bad Request")
    @ApiResponse(responseCode = "409", description = "Record Exist")
    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody OrderItemRequestDTO
                                               orderItemRequestDTO, @Parameter(hidden = true)
                                       @RequestHeader("Authorization") String bearerToken) {
        return orderService.addToCart(orderItemRequestDTO, bearerToken);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves an order using its ID"
    )
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public OrderDAO getSpecificOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @Operation(
            summary = "Get order by Customer ID",
            description = "Retrieves an order using customers ID"
    )
    @ApiResponse(responseCode = "200", description = "Order found By Customer ID")
    @ApiResponse(responseCode = "404", description = "Order not found By customer ID")
    @GetMapping("/by-customer-id/{customerId}")
    public List<OrderDAO> getOrdersByCustomerId(@PathVariable Long customerId) {
        return orderService.getCurrentCart(customerId);
    }

    @Operation(
            summary = "Checkout order",
            description = "Processes checkout for an order - validates stock and decreases inventory"
    )
    @ApiResponse(responseCode = "200", description = "Order checked out successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient stock or invalid order")
    @PostMapping("/{id}/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {
        try {
            OrderResponseDTO checkedOutOrder = orderService.checkout(id, bearerToken);
            return ResponseEntity.ok(checkedOutOrder);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(
            summary = "Clear Cart",
            description = "Clears all items from the user's cart"
    )
    @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    @ApiResponse(responseCode = "400", description = "Failed to clear cart")
    @GetMapping("/clear-cart")
    public ResponseEntity<Void> clearCart() {
        try {
            return orderService.clearCart();
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(
            summary = "Remove product from cart",
            description = "Removes a specific product from the user's cart"
    )
    @ApiResponse(responseCode = "200", description = "Product removed from cart successfully")
    @ApiResponse(responseCode = "400", description = "Product not found in cart")
    @ApiResponse(responseCode = "404", description = "Cart is empty")
    @DeleteMapping("/cart/product/{productId}")
    public ResponseEntity<Void> removeProductFromCart(
            @PathVariable Long productId) {
        try {
            return orderService.removeProductFromCart(productId);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
