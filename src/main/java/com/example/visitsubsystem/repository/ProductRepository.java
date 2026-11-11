package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
