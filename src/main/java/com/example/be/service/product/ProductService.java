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

    @Autowired
    private com.example.be.repository.product.ProductVariantRepository productVariantRepository;

    // --- 1. LẤY TẤT CẢ SẢN PHẨM ---
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithAllDetails();
        return products.stream()
                .map(this::convertToResponse) // Tái sử dụng hàm helper bên dưới
                .collect(Collectors.toList());
    }

    // --- 2. THÊM SẢN PHẨM MỚI
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

        // Save để lấy ID của sản phẩm sinh ra từ DB
        Product savedProduct = productRepository.save(product);

        // 2. LƯU NHIỀU ẢNH PHỤ VÀO BẢNG `product_images`
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {
            for (String url : request.getGalleryImages()) {
                ProductImage productImage = new ProductImage();
                productImage.setProductId(savedProduct.getId());
                productImage.setImageUrl(url);
                productImageRepository.save(productImage);
            }
        }

        // Đọc lại từ DB để có đầy đủ thông tin Category, Brand phục vụ việc map dữ liệu trả về
        Product fullProduct = productRepository.findById(savedProduct.getId())
                .orElse(savedProduct);

        return this.convertToResponse(fullProduct);
    }

    // --- 3. CẬP NHẬT SẢN PHẨM
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        // 1. Lấy dữ liệu cũ đang có trong Database ra
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // 2. CHECK NULL: Nếu trường nào truyền lên có giá trị thì mới đè, không thì GIỮ NGUYÊN
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            product.setBasePrice(request.getBasePrice());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getBrandId() != null) {
            product.setBrandId(request.getBrandId());
        }

        // 3. Xử lý ảnh phụ: Chỉ thay đổi nếu người dùng thực sự truyền mảng galleryImages lên
        if (request.getGalleryImages() != null) {
            // Xóa sạch ảnh phụ cũ
            productImageRepository.deleteByProductId(product.getId());

            // Lưu loạt ảnh phụ mới nếu mảng không rỗng
            if (!request.getGalleryImages().isEmpty()) {
                for (String url : request.getGalleryImages()) {
                    ProductImage productImage = new ProductImage();
                    productImage.setProductId(product.getId());
                    productImage.setImageUrl(url);
                    productImageRepository.save(productImage);
                }
            }
        }

        // 4. Lưu lại sản phẩm sau khi cập nhật chọn lọc
        Product updatedProduct = productRepository.save(product);

        return this.convertToResponse(updatedProduct);
    }

    private ProductResponse convertToResponse(Product product) {
        List<VariantResponse> variantDTOs = null;

        // Map mảng variants sang DTO (chỉ lấy data thuần túy, bỏ trường "product" lặp ngược lại)
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

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0)
                .imageUrl(product.getImageUrl())
                // Tránh lỗi Lazy bằng cách check null an toàn cho Category và Brand
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Không có danh mục")
                .brandName(product.getBrand() != null ? product.getBrand().getName() : "Không có thương hiệu")
                .variants(variantDTOs)
                .build();
    }

    @Transactional
    public void deleteProduct(Integer id) {
        // 1. Kiểm tra sản phẩm có tồn tại trong DB không
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // 2. XÓA BẢNG CON 1: Xóa sạch toàn bộ ảnh phụ trong bảng `product_images`
        productImageRepository.deleteByProductId(product.getId());

        // 3. XÓA BẢNG CON 2: Xóa sạch toàn bộ biến thể trong bảng `product_variants`
        productVariantRepository.deleteByProductId(product.getId());

        // 4. XÓA BẢNG CHA: Cuối cùng, xóa sản phẩm chính khỏi bảng `products`
        productRepository.delete(product);
    }
}