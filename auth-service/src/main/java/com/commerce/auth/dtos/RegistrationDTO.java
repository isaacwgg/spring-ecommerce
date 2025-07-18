package com.commerce.auth.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationDTO(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "firstName is required")
        @Size(min = 3, max = 50, message = "Firstname must be between 3 and 50 characters")
        String firstName,
        @NotBlank(message = "Lastname is required")
        @Size(min = 3, max = 50, message = "Lastname must be between 3 and 50 characters")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(min = 3, max = 50, message = "Email must be between 3 and 50 characters")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {
}