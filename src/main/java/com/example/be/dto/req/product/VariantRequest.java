package com.example.be.dto.req.product;

import lombok.Data;

@Data
public class VariantRequest {
    private String size;
    private String color;
    private Integer stockQuantity;
    private String sku;
}