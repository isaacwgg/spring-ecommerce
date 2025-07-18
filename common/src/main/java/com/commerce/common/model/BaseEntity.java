package com.commerce.common.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@Data
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @CreatedBy
  private Long createdBy;
  @LastModifiedBy
  private Long updatedBy;
  
  @CreatedDate
  private Instant createdAt;
  @LastModifiedDate
  private Instant updatedAt;
  
  @Builder.Default
  @Column(columnDefinition = "boolean default false")
  private boolean deleted = false;
}