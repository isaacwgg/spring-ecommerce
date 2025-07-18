package com.commerce.auth.models;

import com.commerce.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class UserDAO extends BaseEntity {
  @Column(unique = true)
  private String username;
  private String firstName;
  private String lastName;
  @Column(unique = true)
  private String email;
  private String password;
}