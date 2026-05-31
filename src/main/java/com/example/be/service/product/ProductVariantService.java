package com.example.be.service.product;

import com.example.be.dto.req.product.VariantRequest;
import com.example.be.entity.product.Product;
import com.example.be.entity.product.ProductVariant;
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Transactional
    public List<ProductVariant> addVariants(Integer productId, List<VariantRequest> requests) {
        // 1. Kiểm tra sản phẩm gốc có tồn tại không
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // 2. Map từ DTO sang Entity và lưu vào database
        List<ProductVariant> variants = requests.stream().map(req ->
                ProductVariant.builder()
                        .size(req.getSize())
                        .color(req.getColor())
                        .stockQuantity(req.getStockQuantity())
                        .sku(req.getSku())
                        .product(product) // Gắn mối quan hệ ManyToOne
                        .build()
        ).collect(Collectors.toList());

        return variantRepository.saveAll(variants);
    }
}
