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
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.product.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm");
        }

        String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "COD";

        Order order = new Order();
        order.setUserId(request.getUserId());

        if (request.getOrderCode() != null && !request.getOrderCode().trim().isEmpty()) {
            order.setOrderCode(request.getOrderCode());
        } else {
            order.setOrderCode(generateOrderCode());
        }

        order.setShippingName(request.getShippingName());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingAddress(request.getShippingAddress());

        // Gán thông tin địa chỉ GHN vào Entity
        order.setProvinceId(request.getProvinceId());
        order.setDistrictId(request.getDistrictId());
        order.setWardCode(request.getWardCode());

        order.setNotes(request.getNotes());
        order.setPaymentMethod(method);
        order.setStatus("pending");

        // Lúc bấm đặt hàng, tiền chưa trừ nên dù COD hay VNPAY cũng đều là 'unpaid'
        order.setPaymentStatus("unpaid");

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            if (itemRequest.getProductId() == null || itemRequest.getProductVariantId() == null) {
                throw new IllegalArgumentException("ProductId hoặc ProductVariantId trong danh mục không được để trống");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Phiên bản sản phẩm không tồn tại"));

            if (variant.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Không đủ tồn kho cho sản phẩm: " + product.getName());
            }

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

            BigDecimal itemTotal = product.getBasePrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setTotalPrice(itemTotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemTotal);

            // Trừ kho hàng trực tiếp
            variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
            productVariantRepository.save(variant);
        }

        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;

        BigDecimal finalAmount = totalAmount.subtract(discountAmount).add(shippingFee);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setFinalAmount(finalAmount);

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllWithItems();
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus().toLowerCase());
        }

        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus().toLowerCase());
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

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

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if (!"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể xóa đơn hàng có trạng thái pending");
        }

        // Hoàn lại số lượng kho khi xóa đơn hàng chờ xử lý
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProductVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
                    if (variant != null) {
                        variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                        productVariantRepository.save(variant);
                    }
                }
            }
        }

        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if ("shipped".equals(order.getStatus()) || "delivered".equals(order.getStatus())) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng đã được vận chuyển");
        }

        // Hoàn lại số lượng tồn kho khi hủy đơn
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProductVariantId() != null) {
                    ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
                    if (variant != null) {
                        variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                        productVariantRepository.save(variant);
                    }
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
                .shippingFee(order.getShippingFee())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(itemResponses)
                .build();
    }

    private OrderItemResponse convertItemToResponse(OrderItem item) {
        // Tính toán thành tiền trực tiếp hiển thị ra Response DTO
        BigDecimal calculatedTotalPrice = item.getUnitPrice() != null ?
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO;

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId() != null ? Long.valueOf(item.getProductId()) : null)
                .productVariantId(item.getProductVariantId())
                .productName(item.getProductName())
                .variantSku(item.getVariantSku())
                .size(item.getSize())
                .color(item.getColor())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(calculatedTotalPrice)
                .build();
    }

    private String generateOrderCode() {
        String timestamp = System.currentTimeMillis() + "";
        return "ORD" + timestamp.substring(timestamp.length() - 10);
    }

    @Transactional
    public void updatePaymentStatusByOrderCode(String orderCode, String paymentStatus) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderCode));
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
    }
}