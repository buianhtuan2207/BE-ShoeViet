package com.example.be.repository.product;

import com.example.be.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.brand")
    List<Product> findAllWithAllDetails();
}