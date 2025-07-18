package com.commerce.orders.security;

import com.commerce.common.dto.UserResponseDTO;
import com.commerce.common.serviceinvocation.AuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class OrderServiceAuthenticationFilter extends OncePerRequestFilter {
    private final AuthClient authClient;

    public OrderServiceAuthenticationFilter(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null) {
            try {
                UserResponseDTO userResponseDTO = authClient.validate(bearerToken);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userResponseDTO.getId().toString(), null, Collections.emptyList())
                );
            } catch (Exception e) {
                logger.error("Authentication failed", e);
            }
        }

        filterChain.doFilter(request, response);
    }

}
