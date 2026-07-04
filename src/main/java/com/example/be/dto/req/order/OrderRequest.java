package com.example.be.dto.req.order;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private String orderCode;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private String paymentMethod;
    private String shippingName;
    private String fullName;
    private String shippingPhone;
    private String shippingAddress;

    private Integer provinceId;
    private Integer districtId;
    private String wardCode;

    private String notes;
    private List<OrderItemRequest> orderItems;
}