package com.example.be.repository.order;

import com.example.be.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Lấy tất cả các item của một đơn hàng
    List<OrderItem> findByOrderId(Long orderId);
}
