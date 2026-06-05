package com.example.be.dto.req.order;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateOrderStatusRequest {
    private String status; // pending, confirmed, processing, shipped, delivered, cancelled
    private String paymentStatus; // unpaid, paid, refunded
    private String notes;
}
