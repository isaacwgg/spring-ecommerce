package com.commerce.orders.serviceinvocation;

import com.commerce.common.dto.IdsDTO;
import com.commerce.common.dto.ProductDto;
import com.commerce.common.dto.ProductResponseDTO;
import com.commerce.common.dto.StockUpdateBatchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", url = "${product.service.url:}")
public interface ProductClient {
  @GetMapping("/api/products/{id}")
  ProductDto getById(@PathVariable Long id, @RequestHeader("Authorization") String bearerToken);
  
  @GetMapping("/api/products/by-ids")
  List<ProductResponseDTO> getByProductIds(@RequestBody IdsDTO idsDTO, @RequestHeader("Authorization") String bearerToken);
  
  @PostMapping("/api/products/{id}/decrease-stock")
  ProductDto decreaseStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity, @RequestHeader("Authorization") String token);
  
  @PostMapping("/api/products/{id}/increase-stock")
  ProductDto increaseStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity, @RequestHeader("Authorization") String token);
  
  @PostMapping("/api/products/batch/decrease-stock")
  List<ProductDto> batchDecreaseStock(@RequestBody StockUpdateBatchDTO batchDTO, @RequestHeader("Authorization") String token);
}
