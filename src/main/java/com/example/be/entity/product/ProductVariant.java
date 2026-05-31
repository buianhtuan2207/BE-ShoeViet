package com.example.be.entity.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}