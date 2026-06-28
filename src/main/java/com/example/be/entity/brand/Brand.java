package com.example.be.entity.brand;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Entity
@Table(name = "brands")
@Data
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String logo;

    @JsonProperty("is_action")
    @Column(name = "is_action", columnDefinition = "boolean default true")
    private Boolean isAction = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Formula("(SELECT COUNT(*) FROM products p WHERE p.brand_id = id)")
    private Integer productCount;
}
