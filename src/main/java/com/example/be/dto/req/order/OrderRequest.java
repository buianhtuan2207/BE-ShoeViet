package com.example.be.dto.req.order;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
public class OrderRequest {
    private Integer userId;
    private BigDecimal discountAmount;
    private String shippingAddress;
    private String shippingPhone;
    private String shippingName;
    private String notes;
    private List<OrderItemRequest> orderItems;
}
