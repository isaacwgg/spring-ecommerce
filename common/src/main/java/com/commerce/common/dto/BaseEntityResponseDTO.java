package com.commerce.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;


@SuperBuilder(builderMethodName = "baseBuilder")
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntityResponseDTO {
  
  private final boolean deleted = false;
  private Long id;
  private Long createdBy;
  private Long updatedBy;
  private Instant createdAt;
  private Instant updatedAt;
}