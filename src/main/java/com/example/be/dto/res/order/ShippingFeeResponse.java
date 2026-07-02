package com.example.be.dto.res.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingFeeResponse {
    private Double shippingFee;
    private String estimatedDelivery;
}
