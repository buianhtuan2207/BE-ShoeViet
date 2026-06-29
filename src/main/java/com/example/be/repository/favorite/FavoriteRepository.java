package com.example.be.repository.favorite;

import com.example.be.entity.favorite.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Tìm bản ghi yêu thích dựa trên userId và productId
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    // Lấy toàn bộ danh sách sản phẩm yêu thích của 1 user
    List<Favorite> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // Xóa sản phẩm khỏi danh sách yêu thích
    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteAllByUserId(Long userId);
}
