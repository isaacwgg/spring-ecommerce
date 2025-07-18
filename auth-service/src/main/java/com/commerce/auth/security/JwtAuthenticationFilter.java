package com.commerce.auth.security;


import com.commerce.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtProvider;
  private final CustomUserDetailsService userDetailsService;
  private final TokenBlacklistService blacklist;
  
  public JwtAuthenticationFilter(
      JwtTokenProvider jwtProvider,
      CustomUserDetailsService uds,
      TokenBlacklistService blacklist
  ) {
    this.jwtProvider = jwtProvider;
    this.userDetailsService = uds;
    this.blacklist = blacklist;
  }
  
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest req,
      @NonNull HttpServletResponse res,
      @NonNull FilterChain chain
  ) throws ServletException, IOException {
    String header = req.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        if (!blacklist.isBlacklisted(token)) {
          String username = jwtProvider.getUsername(token);
          Long userId = jwtProvider.getUserId(token);
          UserDetails user = userDetailsService.loadUserByUsername(username);
          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(userId.toString(), null, user.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ex) {
        // invalid or expired token: clear context
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(req, res);
  }
}

