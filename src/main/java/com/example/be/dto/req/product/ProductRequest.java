package com.example.be.dto.req.product;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
public class ProductRequest {
    private Integer categoryId;
    private Integer brandId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    // Ảnh đại diện chính (Lưu vào bảng products)
    private String imageUrl;

    // Mảng chứa nhiều ảnh phụ (Lưu vào bảng product_images)
    private List<String> galleryImages;
    private List<VariantRequest> variants;

}