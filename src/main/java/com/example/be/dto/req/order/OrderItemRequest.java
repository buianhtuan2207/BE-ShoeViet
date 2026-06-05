package com.example.be.dto.req.order;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
public class OrderItemRequest {
    private Integer productId;
    private Long productVariantId;
    private Integer quantity;
}
