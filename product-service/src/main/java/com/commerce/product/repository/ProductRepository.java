package com.commerce.product.repository;

import com.commerce.product.models.ProductDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductDAO, Long> {
    List<ProductDAO> findProductByIdIn(List<Long> ids);

    boolean existsByName(String name);
}
