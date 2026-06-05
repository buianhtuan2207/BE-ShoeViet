package com.example.be.controller.order;

import com.example.be.dto.req.order.OrderRequest;
import com.example.be.dto.req.order.UpdateOrderStatusRequest;
import com.example.be.dto.res.order.OrderResponse;
import com.example.be.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ============ CREATE ============

    /**
     * API: POST /api/orders
     * Mô tả: Tạo đơn hàng mới
     * Request: OrderRequest
     * Response: OrderResponse
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            OrderResponse newOrder = orderService.createOrder(request);
            return ResponseEntity.ok(newOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo đơn hàng: " + e.getMessage());
        }
    }

    // ============ READ ============

    /**
     * API: GET /api/orders
     * Mô tả: Lấy tất cả đơn hàng
     * Response: List<OrderResponse>
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * API: GET /api/orders/{id}
     * Mô tả: Lấy chi tiết một đơn hàng theo ID
     * Response: OrderResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API: GET /api/orders/user/{userId}
     * Mô tả: Lấy tất cả đơn hàng của một user
     * Response: List<OrderResponse>
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API: GET /api/orders/code/{orderCode}
     * Mô tả: Lấy đơn hàng theo order code
     * Response: OrderResponse
     */
    @GetMapping("/code/{orderCode}")
    public ResponseEntity<?> getOrderByCode(@PathVariable String orderCode) {
        try {
            OrderResponse order = orderService.getOrderByCode(orderCode);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API: GET /api/orders/status/{status}
     * Mô tả: Lấy đơn hàng theo trạng thái
     * Trạng thái: pending, confirmed, processing, shipped, delivered, cancelled
     * Response: List<OrderResponse>
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // ============ UPDATE ============

    /**
     * API: PUT /api/orders/{id}
     * Mô tả: Cập nhật thông tin đơn hàng (địa chỉ, số điện thoại, ghi chú)
     * Request: OrderRequest
     * Response: OrderResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody OrderRequest request) {
        try {
            OrderResponse updatedOrder = orderService.updateOrder(id, request);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật đơn hàng: " + e.getMessage());
        }
    }

    /**
     * API: PATCH /api/orders/{id}/status
     * Mô tả: Cập nhật trạng thái và trạng thái thanh toán của đơn hàng
     * Request: UpdateOrderStatusRequest
     * Response: OrderResponse
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatus(id, request);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }

    // ============ DELETE ============

    /**
     * API: DELETE /api/orders/{id}
     * Mô tả: Xóa đơn hàng (chỉ xóa được nếu trạng thái là pending)
     * Response: Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok("Đơn hàng đã được xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa đơn hàng: " + e.getMessage());
        }
    }

    /**
     * API: POST /api/orders/{id}/cancel
     * Mô tả: Hủy đơn hàng (thay đổi trạng thái thành cancelled)
     * Response: OrderResponse
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            OrderResponse cancelledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi hủy đơn hàng: " + e.getMessage());
        }
    }
}
