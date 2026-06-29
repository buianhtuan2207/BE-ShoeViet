package com.example.be.controller.favorite;

import com.example.be.service.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    public static class FavoriteRequest {
        public Long productId;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestBody FavoriteRequest request
    ) {
        Long currentUserId = 1L;

        boolean isFavorite = favoriteService.toggleFavorite(currentUserId, request.productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isFavorite", isFavorite);
        response.put("message", isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFavorites() {
        Long currentUserId = 1L;

        List<com.example.be.dto.res.favorite.FavoriteResponse> favoriteProducts = favoriteService.getFavoriteProductsDetails(currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", favoriteProducts.size());
        response.put("data", favoriteProducts);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long productId) {
        Long currentUserId = 1L;

        favoriteService.removeFavorite(currentUserId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa sản phẩm khỏi danh sách yêu thích");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> clearAllFavorites() {
        Long currentUserId = 1L;

        favoriteService.clearAllFavorites(currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa toàn bộ danh sách yêu thích");

        return ResponseEntity.ok(response);
    }
}
