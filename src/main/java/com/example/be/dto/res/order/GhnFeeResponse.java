package com.example.be.dto.res.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GhnFeeResponse {
    private Integer code;
    private String message;
    private GhnFeeData data;

    @Data
    public static class GhnFeeData {
        private Double total;
        @JsonProperty("service_fee")
        private Double serviceFee;
    }
}
