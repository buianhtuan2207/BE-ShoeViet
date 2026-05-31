package com.example.be.controller.product;

import com.example.be.dto.req.product.VariantRequest;
import com.example.be.entity.product.ProductVariant;
import com.example.be.service.product.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> createVariants(
            @PathVariable Integer productId,
            @RequestBody List<VariantRequest> requests) {

        List<ProductVariant> savedVariants = variantService.addVariants(productId, requests);
        return ResponseEntity.ok("Thêm danh sách biến thể thành công! Số lượng: " + savedVariants.size());
    }
}
