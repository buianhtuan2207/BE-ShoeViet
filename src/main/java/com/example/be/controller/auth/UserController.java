package com.example.be.controller.auth;

import com.example.be.dto.req.UpdateProfileRequest;
import com.example.be.entity.User;
import com.example.be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. LẤY THÔNG TIN PROFILE CỦA TÔI
    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile() {
        try {
            String currentEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userService.getUserByEmail(currentEmail);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    // 2. CẬP NHẬT PROFILE CỦA TÔI
    @PutMapping("/my-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            String currentEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User updatedUser = userService.updateProfileByEmail(currentEmail, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 3. XEM CHI TIẾT USER BẤT KỲ THEO ID (Admin dùng)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 4. LẤY TẤT CẢ DANH SÁCH USER (Admin dùng)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
