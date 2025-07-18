package com.commerce.product.service;

import com.commerce.common.dto.IdsDTO;
import com.commerce.common.dto.ProductResponseDTO;
import com.commerce.common.dto.StockUpdateBatchDTO;
import com.commerce.product.dtos.ProductCreateDTO;
import com.commerce.product.dtos.ProductUpdateDTO;
import com.commerce.product.models.ProductDAO;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
  ProductDAO create(ProductCreateDTO productCreateDTO
  );
  
  ProductDAO find(Long id);
  
  
  ResponseEntity<List<ProductResponseDTO>> findProducts(IdsDTO idsDTO);
  
  List<ProductDAO> findAll();
  
  ProductDAO decreaseStock(Long productId, int quantity);
  
  ProductDAO increaseStock(Long productId, int quantity);
  
  ProductDAO update(ProductUpdateDTO productUpdateDTO);
  
  @Transactional
  ResponseEntity<Void> batchDecreaseStock(StockUpdateBatchDTO batchDTO);
}
