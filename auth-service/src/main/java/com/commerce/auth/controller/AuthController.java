package com.commerce.auth.controller;

import com.commerce.auth.dtos.RegistrationDTO;
import com.commerce.auth.models.UserDAO;
import com.commerce.auth.service.TokenBlacklistService;
import com.commerce.auth.service.UserService;
import com.commerce.common.config.utils.AuthenticationUtils;
import com.commerce.common.constants.OpenAPIConstants;
import com.commerce.common.dto.AuthResponseDTO;
import com.commerce.common.dto.LoginRequest;
import com.commerce.common.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final TokenBlacklistService blacklist;
    private final UserService userService;
    private final AuthenticationUtils authUtils;


    public AuthController(
            TokenBlacklistService blacklist,
            UserService userService,
            AuthenticationUtils authUtils) {
        this.blacklist = blacklist;
        this.userService = userService;
        this.authUtils = authUtils;
    }

    @Operation(
            summary = "Validate token",
            description = "Validates if the provided JWT token is valid and not blacklisted"
    )
    @ApiResponse(responseCode = "200", description = "Token is valid and user data returned")
    @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/validate")
    public ResponseEntity<UserResponseDTO> validate() {
        Long userId = authUtils.getCurrentUserId();
        return userService.getByUserID(userId);
    }

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with the provided credentials"
    )
    @ApiResponse(responseCode = "200", description = "User successfully registered")
    @ApiResponse(responseCode = "400", description = "Invalid request body (validation errors)")
    @ApiResponse(responseCode = "403", description = "Invalid registration data (e.g., weak password)")
    @ApiResponse(responseCode = "409", description = "Conflict - Username or email already exists")
    @PostMapping("/register")
    public ResponseEntity<UserDAO> register(@Valid @RequestBody RegistrationDTO registrationDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(registrationDTO));
    }

    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT token"
    )
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "400", description = "Invalid request body (validation errors)")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "403", description = "Invalid login data format")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequest req) {
        AuthResponseDTO response = userService.login(req);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users (requires admin privileges)"
    )
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    @GetMapping("")
    public ResponseEntity<List<UserDAO>> getAll() {
        List<UserDAO> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-username/{username}")
    @Operation(summary = "Get user by username", description = "Get User by username to find specific user record")
    @ApiResponse(responseCode = "200", description = "User found successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDAO> getByUsername(@PathVariable String username) {
        UserDAO user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "User logout",
            description = "Invalidates the current JWT token by adding it to blacklist"
    )
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    @ApiResponse(responseCode = "401", description = "Invalid or missing token")
    @SecurityRequirement(name = OpenAPIConstants.BEARER_SECURITY_SCHEME)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.replace("Bearer ", "").trim();
            blacklist.blacklist(token);
        }
        return ResponseEntity.ok().build();
    }
}

