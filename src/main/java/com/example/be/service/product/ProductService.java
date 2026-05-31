package com.example.be.service.product;

import com.example.be.dto.req.product.ProductRequest;
import com.example.be.dto.res.product.ProductResponse;
import com.example.be.dto.res.variant.VariantResponse;
import com.example.be.entity.product.Product;
import com.example.be.entity.product.ProductImage;
import com.example.be.repository.product.ProductImageRepository;
import com.example.be.repository.product.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithAllDetails();

        return products.stream().map(product -> {

            // 1. Map danh sách biến thể trước (nếu có)
            List<VariantResponse> variantDTOs = null;
            if (product.getVariants() != null) {
                variantDTOs = product.getVariants().stream().map(variant ->
                        VariantResponse.builder() // Giả định VariantResponse cũng dùng @Builder
                                .id(variant.getId())
                                .size(variant.getSize())
                                .color(variant.getColor())
                                .stockQuantity(variant.getStockQuantity())
                                .sku(variant.getSku())
                                .build()
                ).collect(Collectors.toList());
            }

            // 2. Sử dụng @Builder để tạo đối tượng ProductResponse một cách mượt mà
            return ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .basePrice(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0)
                    .imageUrl(product.getImageUrl())

                    // Đọc tên từ bảng liên quan (Có check null an toàn)
                    .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Không có danh mục")
                    .brandName(product.getBrand() != null ? product.getBrand().getName() : "Không có thương hiệu")

                    // Nạp mảng biến thể đã map ở bước 1 vào đây
                    .variants(variantDTOs)
                    .build(); // Kết thúc Builder câu lệnh trả về object hoàn chỉnh

        }).collect(Collectors.toList());
    }

    public Product addProduct(ProductRequest request) {
        // 1. LƯU THÔNG TIN CHUNG VÀO BẢNG `products`
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setImageUrl(request.getImageUrl()); // Ảnh chính

        // Save để lấy ID của sản phẩm sinh ra từ DB
        Product savedProduct = productRepository.save(product);

        // 2. LƯU NHIỀU ẢNH PHỤ VÀO BẢNG `product_images`
        // Kiểm tra xem người dùng có gửi list ảnh phụ lên không
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {

            // Duyệt qua từng đường link ảnh và lưu xuống DB
            for (String url : request.getGalleryImages()) {
                ProductImage productImage = new ProductImage();

                // Trỏ ID về sản phẩm vừa tạo ở bước 1
                productImage.setProductId(savedProduct.getId());
                productImage.setImageUrl(url);

                productImageRepository.save(productImage);
            }
        }

        return savedProduct;
    }
}