package com.example.be.entity.order;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "variant_sku")
    private String variantSku;

    private String size;
    private String color;
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal unitPrice;

    @Transient
    private BigDecimal totalPrice;
}