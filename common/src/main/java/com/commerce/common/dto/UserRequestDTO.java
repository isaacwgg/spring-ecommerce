package com.commerce.common.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Shared representation of a product for inter-service calls.
 */
@Data
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;

}

