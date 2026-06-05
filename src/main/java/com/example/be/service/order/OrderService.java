package com.example.be.service.order;

import com.example.be.dto.req.order.OrderItemRequest;
import com.example.be.dto.req.order.OrderRequest;
import com.example.be.dto.req.order.UpdateOrderStatusRequest;
import com.example.be.dto.res.order.OrderItemResponse;
import com.example.be.dto.res.order.OrderResponse;
import com.example.be.entity.order.Order;
import com.example.be.entity.order.OrderItem;
import com.example.be.entity.product.Product;
import com.example.be.entity.product.ProductVariant;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.order.OrderItemRepository;
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.product.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    // ============ CREATE ============

    /**
     * Tạo đơn hàng mới
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Validate request
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm");
        }

        // 2. Tạo order mới
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setOrderCode(generateOrderCode());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingName(request.getShippingName());
        order.setNotes(request.getNotes());
        order.setStatus("pending");
        order.setPaymentStatus("unpaid");

        // 3. Tính toán tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            // Validate sản phẩm và variant tồn tại
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Phiên bản sản phẩm không tồn tại"));

            // Validate tồn kho
            if (variant.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Không đủ tồn kho cho sản phẩm: " + product.getName());
            }

            // Tạo order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setProductVariantId(itemRequest.getProductVariantId());
            orderItem.setProductName(product.getName());
            orderItem.setVariantSku(variant.getSku());
            orderItem.setSize(variant.getSize());
            orderItem.setColor(variant.getColor());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getBasePrice());

            // Tính tiền item
            BigDecimal itemTotal = product.getBasePrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setTotalPrice(itemTotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemTotal);

            // Giảm tồn kho
            variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
            productVariantRepository.save(variant);
        }

        // 4. Áp dụng giảm giá (nếu có)
        BigDecimal discountAmount = request.getDiscountAmount() != null ?
                request.getDiscountAmount() : BigDecimal.ZERO;

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);

        // 5. Lưu order và order items
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }

    // ============ READ ============

    /**
     * Lấy tất cả đơn hàng
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllWithItems();
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng theo ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        return convertToResponse(order);
    }

    /**
     * Lấy đơn hàng của một user
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Integer userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng theo order code
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        return convertToResponse(order);
    }

    /**
     * Lấy đơn hàng theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ============ UPDATE ============

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    /**
     * Cập nhật đầy đủ thông tin đơn hàng (chỉ cho những thông tin được phép cập nhật)
     */
    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        // Chỉ cho phép cập nhật những trường này (không cập nhật order items)
        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }

        if (request.getShippingPhone() != null) {
            order.setShippingPhone(request.getShippingPhone());
        }

        if (request.getShippingName() != null) {
            order.setShippingName(request.getShippingName());
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    // ============ DELETE ============

    /**
     * Xóa đơn hàng (chỉ xóa được nếu trạng thái là pending)
     */
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        // Chỉ cho phép xóa đơn hàng pending
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể xóa đơn hàng có trạng thái pending");
        }

        // Hoàn lại tồn kho
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                        .orElse(null);
                if (variant != null) {
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
        }

        orderRepository.deleteById(id);
    }

    /**
     * Hủy đơn hàng (thay đổi trạng thái thành cancelled và hoàn lại tồn kho)
     */
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        // Chỉ cho phép hủy nếu chưa shipped
        if ("shipped".equals(order.getStatus()) || "delivered".equals(order.getStatus())) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng đã được vận chuyển");
        }

        // Hoàn lại tồn kho
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                        .orElse(null);
                if (variant != null) {
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
        }

        order.setStatus("cancelled");
        if ("paid".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("refunded");
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    // ============ HELPER METHODS ============

    /**
     * Chuyển đổi Order entity thành OrderResponse
     */
    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(this::convertItemToResponse)
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .shippingPhone(order.getShippingPhone())
                .shippingName(order.getShippingName())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(itemResponses)
                .build();
    }

    /**
     * Chuyển đổi OrderItem entity thành OrderItemResponse
     */
    private OrderItemResponse convertItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productVariantId(item.getProductVariantId())
                .productName(item.getProductName())
                .variantSku(item.getVariantSku())
                .size(item.getSize())
                .color(item.getColor())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    /**
     * Sinh ra order code duy nhất
     */
    private String generateOrderCode() {
        String timestamp = System.currentTimeMillis() + "";
        return "ORD" + timestamp.substring(timestamp.length() - 10);
    }
}
