package com.example.be.dto.req.order;

import lombok.Data;

@Data
public class ShippingFeeRequest {
    private Integer toDistrictId;
    private String toWardCode;

    private Integer weightInGrams;
    private Integer orderTotalValue;
}
