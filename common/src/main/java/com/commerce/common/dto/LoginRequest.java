package com.commerce.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Sent by clients to /api/auth/login
 */
@Data
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  
  @NotBlank
  private String username;
  
  @NotBlank
  private String password;
}

