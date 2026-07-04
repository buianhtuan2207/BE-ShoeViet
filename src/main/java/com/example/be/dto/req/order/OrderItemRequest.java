package com.example.be.dto.req.order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Integer productId;
    private Long productVariantId;
    private Integer quantity;
}