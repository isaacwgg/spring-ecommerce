package com.commerce.product.security;

import com.commerce.common.serviceinvocation.AuthClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity

public class ProductServiceSecurityConfig {
  private final AuthClient authClient;
  
  public ProductServiceSecurityConfig(AuthClient authClient) {
    this.authClient = authClient;
  }
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/favicon.ico").permitAll()
            .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/api/products/**").authenticated()
            .anyRequest().permitAll())
        .addFilterBefore(new ProductServiceAuthenticationFilter(authClient),
            UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
  }
  
}
