package com.commerce.auth.service;

import com.commerce.auth.dtos.RegistrationDTO;
import com.commerce.auth.models.UserDAO;
import com.commerce.common.dto.AuthResponseDTO;
import com.commerce.common.dto.LoginRequest;
import com.commerce.common.dto.UserResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    AuthResponseDTO login(LoginRequest loginRequest);

    UserDAO register(RegistrationDTO registrationDTO);

    List<UserDAO> getUsers();

    UserDAO findByUsername(String username);

    ResponseEntity<UserResponseDTO> getByUserID(Long userId);
}
