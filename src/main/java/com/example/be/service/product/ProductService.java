package com.example.be.service.product;

import com.example.be.dto.req.product.ProductRequest;
import com.example.be.entity.product.Product;
import com.example.be.entity.product.ProductImage;
import com.example.be.repository.product.ProductImageRepository;
import com.example.be.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository; // Tiêm thêm repo ảnh vào đây

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