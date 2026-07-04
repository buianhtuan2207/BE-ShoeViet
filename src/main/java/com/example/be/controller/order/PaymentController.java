package com.example.be.controller.order;

import com.example.be.service.order.OrderService;
import com.example.be.service.order.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    private VNPayService vnpayService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/vnpay-url")
    public ResponseEntity<Map<String, String>> getVNPayUrl(
            @RequestParam long amount,
            @RequestParam String orderCode,
            HttpServletRequest request) {

        String paymentUrl = vnpayService.createPaymentUrl(amount, orderCode, request);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    // Đón kết quả từ VNPAY trả về
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> paymentReturn(HttpServletRequest request) {
        int paymentStatus = vnpayService.orderReturn(request);

        // Lấy lại mã đơn hàng từ VNPAY trả về
        String orderCode = request.getParameter("vnp_TxnRef");

        if (paymentStatus == 1) {
            // Xác thực thành công -> Đổi status đơn hàng sang PAID
            orderService.updatePaymentStatusByOrderCode(orderCode, "paid");
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Thanh toán thành công",
                    "orderCode", orderCode
            ));
        } else if (paymentStatus == 0) {
            // Khách hủy thanh toán hoặc số dư không đủ
            orderService.updatePaymentStatusByOrderCode(orderCode, "failed");
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "message", "Thanh toán thất bại hoặc đã bị hủy"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Chữ ký không hợp lệ!"
            ));
        }
    }
}