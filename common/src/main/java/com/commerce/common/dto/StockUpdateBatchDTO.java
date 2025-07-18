package com.commerce.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateBatchDTO {
  private List<StockUpdateItem> items;
  
  @Data
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StockUpdateItem {
    private Long productId;
    private int quantity;
  }
}