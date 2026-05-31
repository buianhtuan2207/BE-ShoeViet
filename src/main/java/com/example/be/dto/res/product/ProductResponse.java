package com.example.be.dto.res.product;

import com.example.be.dto.res.variant.VariantResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private Double basePrice;
    private String imageUrl;
    private String categoryName;
    private String brandName;
    private List<VariantResponse> variants; // Danh sách size/màu đi kèm
}
