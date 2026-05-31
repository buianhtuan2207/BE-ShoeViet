package com.example.be.repository.product;

import com.example.be.entity.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    // Tìm tất cả biến thể của một sản phẩm nếu cần dùng sau này
    List<ProductVariant> findByProductId(Long productId);
    void deleteByProductId(Integer productId);
}