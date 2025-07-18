package com.commerce.product.service.impl;

import com.commerce.common.dto.IdsDTO;
import com.commerce.common.dto.ProductResponseDTO;
import com.commerce.common.dto.StockUpdateBatchDTO;
import com.commerce.common.exception.ConflictException;
import com.commerce.common.exception.ResourceNotFoundException;
import com.commerce.product.dtos.ProductCreateDTO;
import com.commerce.product.dtos.ProductUpdateDTO;
import com.commerce.product.models.ProductDAO;
import com.commerce.product.repository.ProductRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDAO testProduct;
    private ProductCreateDTO productCreateDTO;
    private ProductUpdateDTO productUpdateDTO;

    @BeforeEach
    void setUp() {
        testProduct = new ProductDAO();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setStock(100);

        productCreateDTO = new ProductCreateDTO();
        productCreateDTO.setName("Test Product");
        productCreateDTO.setPrice(BigDecimal.valueOf(99.99));
        productCreateDTO.setStock(100);

        productUpdateDTO = new ProductUpdateDTO(1L, "Updated Product", BigDecimal.valueOf(199.99), 200);
    }

    @Test
    void create_Successful() {
        // Arrange
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(modelMapper.map(productCreateDTO, ProductDAO.class)).thenReturn(testProduct);
        when(productRepository.save(any(ProductDAO.class))).thenReturn(testProduct);

        // Act
        ProductDAO result = productService.create(productCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository).existsByName("Test Product");
        verify(productRepository).save(any(ProductDAO.class));
    }

    @Test
    void create_ProductNameExists() {
        // Arrange
        when(productRepository.existsByName("Test Product")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> productService.create(productCreateDTO));
    }

    @Test
    void find_Successful() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductDAO result = productService.find(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void find_NotFound() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.find(99L));
    }

    @Test
    void findProducts_Successful() {
        // Arrange
        List<Long> ids = List.of(1L, 2L, 3L);
        IdsDTO idsDTO = new IdsDTO(ids);
        List<ProductDAO> products = List.of(testProduct);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1L, "Test Product", BigDecimal.valueOf(99.99), 100);

        when(productRepository.findProductByIdIn(ids)).thenReturn(products);
        when(modelMapper.map(testProduct, ProductResponseDTO.class)).thenReturn(responseDTO);

        // Act
        ResponseEntity<List<ProductResponseDTO>> response = productService.findProducts(idsDTO);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void findProducts_EmptyList() {
        // Arrange
        IdsDTO idsDTO = new IdsDTO(List.of());
        when(productRepository.findProductByIdIn(List.of())).thenReturn(List.of());

        // Act
        ResponseEntity<List<ProductResponseDTO>> response = productService.findProducts(idsDTO);

        // Assert
        assertNotNull(response);
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void findAll_Successful() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        // Act
        List<ProductDAO> result = productService.findAll();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void decreaseStock_Successful() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductDAO.class))).thenReturn(testProduct);

        // Act
        ProductDAO result = productService.decreaseStock(1L, 10);

        // Assert
        assertNotNull(result);
        assertEquals(90, result.getStock());
    }

    @Test
    void decreaseStock_InsufficientStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(ServiceException.class, () -> productService.decreaseStock(1L, 101));
    }

    @Test
    void increaseStock_Successful() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductDAO.class))).thenReturn(testProduct);

        // Act
        ProductDAO result = productService.increaseStock(1L, 50);

        // Assert
        assertNotNull(result);
        assertEquals(150, result.getStock());
    }

    @Test
    void update_Successful() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductDAO.class))).thenReturn(testProduct);

        // Act
        ProductDAO result = productService.update(productUpdateDTO);

        // Assert
        assertNotNull(result);
        verify(modelMapper).map(productUpdateDTO, testProduct);
        verify(productRepository).save(testProduct);
    }

    @Test
    void update_ProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> productService.update(productUpdateDTO));
    }

    @Test
    void batchDecreaseStock_Successful() {
        // Arrange
        StockUpdateBatchDTO.StockUpdateItem item1 = new StockUpdateBatchDTO.StockUpdateItem(1L, 10);
        StockUpdateBatchDTO batchDTO = new StockUpdateBatchDTO(List.of(item1));

        when(productRepository.findProductByIdIn(List.of(1L))).thenReturn(List.of(testProduct));
        when(productRepository.saveAll(anyList())).thenReturn(List.of(testProduct));

        // Act
        ResponseEntity<Void> response = productService.batchDecreaseStock(batchDTO);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(90, testProduct.getStock());
    }

    @Test
    void batchDecreaseStock_ProductNotFound() {
        // Arrange
        StockUpdateBatchDTO.StockUpdateItem item1 = new StockUpdateBatchDTO.StockUpdateItem(99L, 10);
        StockUpdateBatchDTO batchDTO = new StockUpdateBatchDTO(List.of(item1));

        when(productRepository.findProductByIdIn(List.of(99L))).thenReturn(List.of());

        // Act & Assert
        assertThrows(ServiceException.class, () -> productService.batchDecreaseStock(batchDTO));
    }

    @Test
    void batchDecreaseStock_InsufficientStock() {
        // Arrange
        StockUpdateBatchDTO.StockUpdateItem item1 = new StockUpdateBatchDTO.StockUpdateItem(1L, 101);
        StockUpdateBatchDTO batchDTO = new StockUpdateBatchDTO(List.of(item1));

        when(productRepository.findProductByIdIn(List.of(1L))).thenReturn(List.of(testProduct));

        // Act & Assert
        assertThrows(ServiceException.class, () -> productService.batchDecreaseStock(batchDTO));
    }
}