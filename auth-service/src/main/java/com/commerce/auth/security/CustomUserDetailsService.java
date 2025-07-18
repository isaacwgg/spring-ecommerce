package com.commerce.auth.security;

import com.commerce.auth.models.UserDAO;
import com.commerce.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository repo;
  
  public CustomUserDetailsService(UserRepository r) {
    this.repo = r;
  }
  
  @Override
  public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException {
    UserDAO u = repo.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
        u.getUsername(),
        u.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }
}