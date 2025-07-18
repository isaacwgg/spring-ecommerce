package com.commerce.auth.service.impl;

import com.commerce.auth.dtos.RegistrationDTO;
import com.commerce.auth.models.UserDAO;
import com.commerce.auth.repository.UserRepository;
import com.commerce.auth.security.JwtTokenProvider;
import com.commerce.auth.service.UserService;
import com.commerce.common.dto.AuthResponseDTO;
import com.commerce.common.dto.LoginRequest;
import com.commerce.common.dto.UserResponseDTO;
import com.commerce.common.exception.ConflictException;
import com.commerce.common.exception.InvalidCredentialsException;
import com.commerce.common.exception.ResourceNotFoundException;
import com.commerce.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;

    public UserServiceImpl(UserRepository repo, PasswordEncoder encoder, ModelMapper modelMapper, AuthenticationManager authManager, UserRepository userRepository, JwtTokenProvider jwtProvider) {
        this.repo = repo;
        this.encoder = encoder;
        this.modelMapper = modelMapper;
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public AuthResponseDTO login(LoginRequest loginRequest) {
        try {
            // Attempt authentication
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // If authentication succeeds, get user details
            UserDAO userDAO = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        log.error("User not found after successful authentication: {}", loginRequest.getUsername());
                        return new ResourceNotFoundException("User not found");
                    });

            String token = jwtProvider.createToken(userDAO);
            return AuthResponseDTO.builder()
                    .token(token)
                    .user(modelMapper.map(userDAO, UserResponseDTO.class))
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername());
            throw new com.commerce.common.exception.BadCredentialsException("Invalid username or password");
        } catch (AuthenticationException ex) {
            log.error("Authentication error for user: {}", loginRequest.getUsername(), ex);
            throw new InvalidCredentialsException("Authentication failed");
        }
    }


    @Override
    public UserDAO register(RegistrationDTO registrationDTO) {
        // Check if username exists
        Optional<UserDAO> existingUser = repo.findByUsername(registrationDTO.username());
        if (existingUser.isPresent()) {
            throw new ConflictException("Username already exists");
        }

        // Check if email exists
        existingUser = repo.findByEmail(registrationDTO.email());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email already exists");
        }

        // Validate registration data
        if (registrationDTO.password() == null || registrationDTO.password().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        try {
            UserDAO u = new UserDAO();
            u.setUsername(registrationDTO.username());
            u.setFirstName(registrationDTO.firstName());
            u.setLastName(registrationDTO.lastName());
            u.setEmail(registrationDTO.email());
            u.setUsername(registrationDTO.username());
            u.setPassword(encoder.encode(registrationDTO.password()));
            return repo.save(u);
        } catch (Exception e) {
            log.info("Error during registration for user: {}", registrationDTO.username(), e);
            throw new ValidationException("Error during registration: " + e.getMessage());
        }
    }

    @Override
    public List<UserDAO> getUsers() {
        try {
            return repo.findAll();
        } catch (Exception e) {
            throw new ValidationException("Error retrieving users");
        }
    }

    @Override
    public UserDAO findByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public ResponseEntity<UserResponseDTO> getByUserID(Long userId) {
        UserDAO userDAO = repo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return ResponseEntity.ok(
                modelMapper.map(userDAO, UserResponseDTO.class)
        );
    }
}

