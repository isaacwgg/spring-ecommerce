package com.commerce.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    UserResponseDTO user;
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
}