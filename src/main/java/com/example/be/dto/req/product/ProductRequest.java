package com.example.be.dto.req.product;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
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

}