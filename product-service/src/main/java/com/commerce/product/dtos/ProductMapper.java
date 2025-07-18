package com.commerce.product.dtos;

import com.commerce.common.dto.ProductDto;
import com.commerce.product.models.ProductDAO;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
  public ProductDto toDto(ProductDAO productDAO) {
    return ProductDto.builder()
        .id(productDAO.getId())
        .name(productDAO.getName())
        .price(productDAO.getPrice())
        .stock(productDAO.getStock())
        .build();
  }
  
  public ProductDAO toEntity(ProductDto dto) {
    return ProductDAO.builder()
        .id(dto.getId())
        .name(dto.getName())
        .price(dto.getPrice())
        .stock(dto.getStock())
        // Other fields
        .build();
  }
}
