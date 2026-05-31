package com.example.be.repository.product;

import com.example.be.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Spring Data JPA tự động hàm save()
}