package com.commerce.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO extends BaseEntityResponseDTO {
  private Long id;
  private String firstName;
  
  private String lastName;
  private String email;
  
}

