package com.example.be.service.favorite;

import com.example.be.dto.res.favorite.FavoriteResponse;
import com.example.be.entity.favorite.Favorite;
import com.example.be.entity.product.Product;
import com.example.be.repository.favorite.FavoriteRepository;
import com.example.be.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final ProductRepository productRepository;

    @Transactional
    public boolean toggleFavorite(Long userId, Long productId) {
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndProductId(userId, productId);

        if (existingFavorite.isPresent()) {
            // Nếu đã tồn tại -> User muốn bỏ yêu thích
            favoriteRepository.delete(existingFavorite.get());
            return false; // Trả về false nghĩa là trái tim đã tắt (unliked)
        } else {
            // Nếu chưa tồn tại -> Thêm vào danh sách yêu thích
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(userId);
            newFavorite.setProductId(productId);
            favoriteRepository.save(newFavorite);
            return true; // Trả về true nghĩa là trái tim đã sáng (liked)
        }
    }

    @Transactional(readOnly = true)
    public List<Long> getFavoriteProductIds(Long userId) {
        // Lấy danh sách ID sản phẩm mà user này đã thích
        List<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.example.be.dto.res.favorite.FavoriteResponse> getFavoriteProductsDetails(Long userId) {
        // 1. Lấy danh sách các bản ghi yêu thích của User từ FavoriteRepository
        List<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        // 2. Duyệt qua danh sách để lấy thông tin chi tiết từng sản phẩm
        return favorites.stream()
                .map(favorite -> {
                    // Ép kiểu productId từ Long sang Integer (.intValue()) để khớp với ProductRepository<Product, Integer>
                    Integer idInRepository = favorite.getProductId().intValue();

                    // Tìm kiếm sản phẩm trong Database
                    Optional<Product> productOpt = productRepository.findById(idInRepository);

                    if (productOpt.isPresent()) {
                        Product p = productOpt.get();

                        // Khởi tạo DTO FavoriteResponse trùng khớp với Front-End
                        // Bạn hãy kiểm tra lại các hàm getter của Entity Product (p.getName(), p.getBrand(), v.v...) xem khớp chưa nhé
                        return new com.example.be.dto.res.favorite.FavoriteResponse(
                                p.getId().longValue(), // Chuyển ngược lại Long cho đúng cấu trúc DTO
                                p.getName(),
                                p.getBrand() != null ? p.getBrand().getName() : "Thương hiệu",
                                p.getCategory() != null ? p.getCategory().getName() : "Giày thể thao",
                                p.getBasePrice() != null ? p.getBasePrice().doubleValue() : 0.0, // Đảm bảo thuộc tính này trong Entity Product đang là Double
                                p.getImageUrl(),  // Đảm bảo thuộc tính này trong Entity Product đang là String
                                true
                        );
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull) // Loại bỏ các sản phẩm rỗng (nếu sản phẩm đó đã bị xóa khỏi hệ thống)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearAllFavorites(Long userId) {
        favoriteRepository.deleteAllByUserId(userId);
    }
}
