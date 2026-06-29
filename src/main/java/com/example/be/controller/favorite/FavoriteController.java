package com.example.be.controller.favorite;

import com.example.be.entity.User;
import com.example.be.service.UserService;
import com.example.be.service.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    public static class FavoriteRequest {
        public Long productId;
    }

    private Long getCurrentUserId() {
        String currentEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(currentEmail);
        if (user == null || user.getId() == null) {
            throw new RuntimeException("Không tìm thấy thông tin tài khoản hợp lệ.");
        }
        return user.getId().longValue();
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleFavorite(
            @RequestBody FavoriteRequest request
    ) {
        try {
            Long currentUserId = getCurrentUserId();

            boolean isFavorite = favoriteService.toggleFavorite(currentUserId, request.productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isFavorite", isFavorite);
            response.put("message", isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getLocalizedMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getFavorites() {
        try {
            Long currentUserId = getCurrentUserId();

            List<com.example.be.dto.res.favorite.FavoriteResponse> favoriteProducts =
                    favoriteService.getFavoriteProductsDetails(currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", favoriteProducts.size());
            response.put("data", favoriteProducts);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getLocalizedMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long productId) {
        try {
            Long currentUserId = getCurrentUserId();

            favoriteService.removeFavorite(currentUserId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi danh sách yêu thích");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getLocalizedMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> clearAllFavorites() {
        try {
            Long currentUserId = getCurrentUserId();

            favoriteService.clearAllFavorites(currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa toàn bộ danh sách yêu thích");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getLocalizedMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}