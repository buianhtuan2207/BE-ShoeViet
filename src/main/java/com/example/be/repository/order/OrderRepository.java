package com.example.be.repository.order;

import com.example.be.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Lấy tất cả đơn hàng của một user
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.userId = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserId(@Param("userId") Integer userId);
    
    // Lấy đơn hàng theo order code
    Optional<Order> findByOrderCode(String orderCode);
    
    // Lấy đơn hàng theo id với chi tiết đầy đủ
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    // Lấy tất cả đơn hàng theo trạng thái
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.status = :status " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByStatus(@Param("status") String status);
    
    // Lấy tất cả đơn hàng
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllWithItems();
}
