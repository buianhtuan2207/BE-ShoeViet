package com.example.be.controller.order;

import com.example.be.dto.req.order.ShippingFeeRequest;
import com.example.be.dto.res.order.ShippingFeeResponse;
import com.example.be.service.order.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipping")
@CrossOrigin("*")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    // FE gọi API này để lấy danh sách Tỉnh/Thành
    @GetMapping("/provinces")
    public ResponseEntity<Object> getProvinces() {
        return ResponseEntity.ok(shippingService.getProvinces());
    }

    // FE gọi API này truyền provinceId để lấy Quận/Huyện
    @GetMapping("/districts")
    public ResponseEntity<Object> getDistricts(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(shippingService.getDistricts(provinceId));
    }

    // FE gọi API này truyền districtId để lấy Phường/Xã
    @GetMapping("/wards")
    public ResponseEntity<Object> getWards(@RequestParam Integer districtId) {
        return ResponseEntity.ok(shippingService.getWards(districtId));
    }

    // FE gọi API này để tính tiền Ship khi User chọn xong địa chỉ
    @PostMapping("/calculate-fee")
    public ResponseEntity<ShippingFeeResponse> calculateFee(@RequestBody ShippingFeeRequest req) {
        ShippingFeeResponse response = shippingService.calculateShippingFee(req);
        return ResponseEntity.ok(response);
    }
}