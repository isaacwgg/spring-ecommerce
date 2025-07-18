package com.commerce.product.controller;

import com.commerce.common.constants.OpenAPIConstants;
import com.commerce.common.dto.IdsDTO;
import com.commerce.common.dto.ProductDto;
import com.commerce.common.dto.ProductResponseDTO;
import com.commerce.common.dto.StockUpdateBatchDTO;
import com.commerce.product.dtos.ProductCreateDTO;
import com.commerce.product.dtos.ProductMapper;
import com.commerce.product.dtos.ProductUpdateDTO;
import com.commerce.product.models.ProductDAO;
import com.commerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Controller", description = "APIs for product management")
@Slf4j
@SecurityRequirement(name = OpenAPIConstants.BEARER_SECURITY_SCHEME)
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService s, ProductMapper productMapper) {
        this.productService = s;
        this.productMapper = productMapper;
    }

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product with the provided details"
    )
    @ApiResponse(responseCode = "200", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid product data")
    @ApiResponse(responseCode = "403", description = "Bad Request")
    @ApiResponse(responseCode = "409", description = "Record Exist")
    @PostMapping("/create")
    public ProductDAO create(@RequestBody ProductCreateDTO productCreateDTO) {
        return productService.create(productCreateDTO);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product using its ID"
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @GetMapping("/{id}")
    public ProductDAO get(@PathVariable Long id) {
        return productService.find(id);
    }

    @ApiResponse(responseCode = "200", description = "Product found")
    @GetMapping("/by-ids")
    public ResponseEntity<List<ProductResponseDTO>> get(@RequestBody IdsDTO ids) {
        return productService.findProducts(ids);
    }

    @Operation(
            summary = "Update product",
            description = "Updates an existing product with the provided details"
    )


    @PostMapping("/update")
    public ProductDAO update(@RequestBody ProductUpdateDTO productUpdateDTO) {
        return productService.update(productUpdateDTO);
    }

    @Operation(
            summary = "Decrease product stock",
            description = "Decreases product stocks"
    )
    @ApiResponse(responseCode = "200", description = "Product stock decreased successfully")
    @ApiResponse(responseCode = "400", description = "Decreased failed")
    @PostMapping("/{id}/decrease-stock")
    public ResponseEntity<ProductDto> decreaseStock(@PathVariable Long id, @RequestParam int quantity, @RequestHeader("Authorization") String token) {
        if (quantity <= 0) {
            log.error("Invalid quantity: {} must be positive", quantity);
            throw new IllegalArgumentException("Quantity must be positive");
        }
        ProductDAO productDAO = productService.decreaseStock(id, quantity);
        return ResponseEntity.ok(productMapper.toDto(productDAO));
    }

    @ApiResponse(responseCode = "200", description = "Product stock increased successfully")
    @ApiResponse(responseCode = "400", description = "Increased failed")
    @PostMapping("/{id}/increase-stock")
    public ResponseEntity<ProductDto> increaseStock(@PathVariable Long id, @RequestParam int quantity, @RequestHeader("Authorization") String token) {
        if (quantity <= 0) {
            log.error("Invalid quantity must be positive: {}", quantity);
            throw new IllegalArgumentException("Quantity must be positive");
        }
        ProductDAO productDAO = productService.increaseStock(id, quantity);
        return ResponseEntity.ok(productMapper.toDto(productDAO));
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieves a list of all available products"
    )
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    @GetMapping
    public List<ProductDAO> all() {
        return productService.findAll();
    }

    @Operation(
            summary = "Batch decrease product stock",
            description = "Decreases stock for multiple products in a single request"
    )
    @ApiResponse(responseCode = "200", description = "Stock decreased successfully for all products")
    @ApiResponse(responseCode = "400", description = "Batch stock decrease failed")
    @PostMapping("/batch/decrease-stock")
    public ResponseEntity<Void> batchDecreaseStock(@RequestBody StockUpdateBatchDTO batchDTO) {
        return productService.batchDecreaseStock(batchDTO);
    }

}
