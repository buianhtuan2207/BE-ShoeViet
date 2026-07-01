package com.example.be.service.product;

import com.example.be.dto.req.product.ProductRequest;
import com.example.be.dto.req.product.VariantRequest;
import com.example.be.dto.res.product.ProductResponse;
import com.example.be.dto.res.variant.VariantResponse;
import com.example.be.entity.favorite.Favorite;
import com.example.be.entity.product.Product;
import com.example.be.entity.product.ProductImage;
import com.example.be.entity.product.ProductVariant;
import com.example.be.repository.favorite.FavoriteRepository;
import com.example.be.repository.product.ProductImageRepository;
import com.example.be.repository.product.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private com.example.be.repository.product.ProductVariantRepository productVariantRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    // --- 1. LẤY TẤT CẢ SẢN PHẨM (Đã sửa lỗi cú pháp & tích hợp kiểm tra isLiked) ---
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Long currentUserId) {
        // Lấy toàn bộ sản phẩm kèm chi tiết từ DB
        List<Product> products = productRepository.findAllWithAllDetails();

        // Tạo danh sách lưu ID các sản phẩm đã thích nếu user đã đăng nhập
        List<Long> favoriteProductIds = new ArrayList<>();
        if (currentUserId != null) {
            favoriteProductIds = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId)
                    .stream()
                    .map(Favorite::getProductId)
                    .collect(Collectors.toList());
        }

        final List<Long> finalFavIds = favoriteProductIds;

        // Tiến hành map sang DTO Response
        return products.stream()
                .map(product -> {
                    ProductResponse response = this.convertToResponse(product);
                    // Đặt trạng thái isLiked: true nếu ID sản phẩm nằm trong list đã thích
                    response.setLiked(finalFavIds.contains(product.getId().longValue()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    // --- 2. THÊM SẢN PHẨM MỚI ---
    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setImageUrl(request.getImageUrl());

        Product savedProduct = productRepository.save(product);

        List<ProductImage> savedGallery = new ArrayList<>();
        if (request.getGalleryImages() != null && !request.getGalleryImages().isEmpty()) {
            for (String url : request.getGalleryImages()) {
                ProductImage productImage = new ProductImage();
                productImage.setProductId(savedProduct.getId());
                productImage.setProduct(savedProduct);
                productImage.setImageUrl(url);
                savedGallery.add(productImageRepository.save(productImage));
            }
        }
        savedProduct.setProductImages(savedGallery);

        List<ProductVariant> savedVariants = new ArrayList<>();
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (VariantRequest vRequest : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setSize(vRequest.getSize());
                variant.setColor(vRequest.getColor());
                variant.setStockQuantity(vRequest.getStockQuantity());
                variant.setSku(vRequest.getSku());

                String skuInput = vRequest.getSku();
                if (skuInput == null || skuInput.trim().isEmpty()) {
                    String generatedSku = "PRD" + savedProduct.getId() + "-"
                            + vRequest.getColor().toUpperCase().replaceAll("\\s+", "") + "-"
                            + vRequest.getSize().toUpperCase() + "-"
                            + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                    variant.setSku(generatedSku);
                } else {
                    variant.setSku(skuInput);
                }
                savedVariants.add(productVariantRepository.save(variant));
            }
        }
        savedProduct.setVariants(savedVariants);

        Product fullProduct = productRepository.findById(savedProduct.getId()).orElse(savedProduct);
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
            product.setProductImages(newGallery);
        }

        Product updatedProduct = productRepository.save(product);
        return this.convertToResponse(updatedProduct);
    }

    // --- 4. HÀM HELPER CHUYỂN ĐỔI ENTITY SANG DTO ---
    private ProductResponse convertToResponse(Product product) {
        List<VariantResponse> variantDTOs = null;

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

        List<String> galleryUrls = new ArrayList<>();
        if (product.getProductImages() != null) {
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
                .galleryImages(galleryUrls)
                .isLiked(false) // Mặc định là false, sẽ được ghi đè giá trị thực tế tại hàm getAllProducts()
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
    public ProductResponse getProductById(Integer id, Long currentUserId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        ProductResponse response = this.convertToResponse(product);

        // Đánh dấu thích cho trang chi tiết nếu có ID người dùng
        if (currentUserId != null) {
            boolean isLiked = favoriteRepository.findByUserIdAndProductId(currentUserId, id.longValue()).isPresent();
            response.setLiked(isLiked);
        }

        return response;
    }
}