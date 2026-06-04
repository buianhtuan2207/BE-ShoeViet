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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private com.example.be.repository.product.ProductVariantRepository productVariantRepository;

    // --- 1. LẤY TẤT CẢ SẢN PHẨM (Đã tự động lấy kèm list ảnh phụ qua hàm helper) ---
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithAllDetails();
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // --- 2. THÊM SẢN PHẨM MỚI (Đã sửa lỗi lưu và hiển thị ảnh phụ đồng bộ) ---
    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        // 1. LƯU THÔNG TIN CHUNG VÀO BẢNG `products`
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setImageUrl(request.getImageUrl()); // Ảnh chính

        // Lưu trước để lấy ID sản phẩm sinh ra từ DB
        Product savedProduct = productRepository.save(product);

        // 2. LƯU NHIỀU ẢNH PHỤ VÀO BẢNG `product_images` VÀ THIẾT LẬP KẾT NỐI NGƯỢC
        List<ProductImage> savedGallery = new ArrayList<>();
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {
            for (String url : request.getGalleryImages()) {
                ProductImage productImage = new ProductImage();
                productImage.setProductId(savedProduct.getId());
                productImage.setProduct(savedProduct); // Đảm bảo mapping mối quan hệ JPA 2 chiều vững chắc
                productImage.setImageUrl(url);

                savedGallery.add(productImageRepository.save(productImage));
            }
        }

        // Gán ngược danh sách vừa lưu vào Entity để Hibernate không bị rỗng khi Convert sang Response
        savedProduct.setProductImages(savedGallery);

        // Khôi phục lấy thông tin danh mục, thương hiệu đầy đủ từ DB
        Product fullProduct = productRepository.findById(savedProduct.getId())
                .orElse(savedProduct);

        return this.convertToResponse(fullProduct);
    }

    // --- 3. CẬP NHẬT SẢN PHẨM ---
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getBrandId() != null) product.setBrandId(request.getBrandId());

        // Xử lý ảnh phụ
        if (request.getGalleryImages() != null) {
            productImageRepository.deleteByProductId(product.getId());

            List<ProductImage> newGallery = new ArrayList<>();
            if (!request.getGalleryImages().isEmpty()) {
                for (String url : request.getGalleryImages()) {
                    ProductImage productImage = new ProductImage();
                    productImage.setProductId(product.getId());
                    productImage.setProduct(product);
                    productImage.setImageUrl(url);
                    newGallery.add(productImageRepository.save(productImage));
                }
            }
            product.setProductImages(newGallery); // Cập nhật lại context hiện tại cho thực thể
        }

        Product updatedProduct = productRepository.save(product);
        return this.convertToResponse(updatedProduct);
    }

    // --- 4. HÀM HELPER CHUYỂN ĐỔI ENTITY SANG DTO (Sửa đổi cốt lõi bổ sung Map danh sách ảnh phụ) ---
    private ProductResponse convertToResponse(Product product) {
        List<VariantResponse> variantDTOs = null;

        // Map mảng variants sang DTO
        if (product.getVariants() != null) {
            variantDTOs = product.getVariants().stream().map(variant ->
                    VariantResponse.builder()
                            .id(variant.getId())
                            .size(variant.getSize())
                            .color(variant.getColor())
                            .stockQuantity(variant.getStockQuantity())
                            .sku(variant.getSku())
                            .build()
            ).collect(Collectors.toList());
        }

        // BỔ SUNG: Trích xuất mảng danh sách String từ thực thể ProductImages để map sang DTO
        List<String> galleryUrls = new ArrayList<>();
        if (product.getProductImages() != null) { // Hãy kiểm tra xem trong Entity tên thuộc tính là getProductImages() hay đặt khác nhé
            galleryUrls = product.getProductImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0)
                .imageUrl(product.getImageUrl())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Không có danh mục")
                .brandName(product.getBrand() != null ? product.getBrand().getName() : "Không có thương hiệu")
                .variants(variantDTOs)
                .galleryImages(galleryUrls) // BẮT BUỘC PHẢI TRẢ VỀ ĐÂY để Frontend nhận được danh sách ảnh phụ
                .build();
    }

    // --- 5. XÓA SẢN PHẨM ---
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        productImageRepository.deleteByProductId(product.getId());
        productVariantRepository.deleteByProductId(product.getId());
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Tái sử dụng hàm helper convertToResponse đã có sẵn của bạn để map đầy đủ ảnh phụ và variants
        return this.convertToResponse(product);
    }
}