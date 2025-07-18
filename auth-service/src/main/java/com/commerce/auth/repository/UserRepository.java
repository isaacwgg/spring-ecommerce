package com.commerce.auth.repository;

import com.commerce.auth.models.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDAO, Long> {
  Optional<UserDAO> findByUsername(String username);
  
  Optional<UserDAO> findByEmail(String email);
}

