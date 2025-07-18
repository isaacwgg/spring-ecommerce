package com.commerce.auth.service.impl;

import com.commerce.auth.dtos.RegistrationDTO;
import com.commerce.auth.models.UserDAO;
import com.commerce.auth.repository.UserRepository;
import com.commerce.auth.security.JwtTokenProvider;
import com.commerce.common.dto.AuthResponseDTO;
import com.commerce.common.dto.LoginRequest;
import com.commerce.common.dto.UserResponseDTO;
import com.commerce.common.exception.ConflictException;
import com.commerce.common.exception.ResourceNotFoundException;
import com.commerce.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDAO testUser;
    private RegistrationDTO registrationDTO;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserDAO();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        registrationDTO = new RegistrationDTO(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123"
        );

        loginRequest = new LoginRequest("testuser", "password123");
    }

    @Test
    void login_Successful() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.createToken(testUser)).thenReturn("jwtToken");
        when(modelMapper.map(testUser, UserResponseDTO.class)).thenReturn(new UserResponseDTO());

        // Act
        AuthResponseDTO response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void login_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequest));
    }

    @Test
    void login_UserNotFoundAfterAuthentication() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.login(loginRequest));
    }

    @Test
    void register_Successful() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserDAO.class))).thenReturn(testUser);

        // Act
        UserDAO result = userService.register(registrationDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(UserDAO.class));
    }

    @Test
    void register_UsernameExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(ConflictException.class, () -> userService.register(registrationDTO));
    }

    @Test
    void register_EmailExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(ConflictException.class, () -> userService.register(registrationDTO));
    }

    @Test
    void register_InvalidPassword() {
        // Arrange
        RegistrationDTO invalidPasswordDTO = new RegistrationDTO(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "123"
        );

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.register(invalidPasswordDTO));
    }

    @Test
    void getUsers_Successful() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // Act
        List<UserDAO> result = userService.getUsers();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getUsers_ExceptionThrown() {
        // Arrange
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.getUsers());
    }

    @Test
    void findByUsername_Successful() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDAO result = userService.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.findByUsername("nonexistent"));
    }

    @Test
    void getByUserID_Successful() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserResponseDTO.class)).thenReturn(new UserResponseDTO());

        // Act
        var response = userService.getByUserID(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getByUserID_NotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getByUserID(99L));
    }
}