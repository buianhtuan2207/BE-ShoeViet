package com.example.be.dto.req.product;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class VariantRequest {
    private String size;
    private String color;
    private Integer stockQuantity;
    private String sku;
}