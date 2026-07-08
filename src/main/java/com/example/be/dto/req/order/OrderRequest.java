package com.example.be.dto.req.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private String orderCode;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    @NotBlank(message = "Tên người nhận hàng không được để trống")
    private String shippingName;

    private String fullName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại nhận hàng không đúng định dạng Việt Nam")
    private String shippingPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @NotNull(message = "Mã tỉnh/thành phố không được để trống")
    private Integer provinceId;

    @NotNull(message = "Mã quận/huyện không được để trống")
    private Integer districtId;

    @NotBlank(message = "Mã phường/xã không được để trống")
    private String wardCode;

    private String notes;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderItemRequest> orderItems;
}