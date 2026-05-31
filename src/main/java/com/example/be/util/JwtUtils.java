package com.example.be.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtils {

    private final String JWT_SECRET = "9aF8b2C7dE1fG4hI3jK5lM2nO6pQ8rS0tU2vW4xY6zA1b3c5d7e9f0a1b2c3d4e5";
    private final long JWT_EXPIRATION = 3600000L;

    public String generateToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

        return Jwts.builder()
                .subject(email)
                .claim("role", role) // Lưu kèm quyền của user vào token
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) // Sẽ hết hạn sau đúng 1 giờ
                .signWith(key)
                .compact();
    }
    // 1. Hàm lấy toàn bộ thông tin (Claims) bên trong Token ra
    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Hàm trích xuất Email (Subject) ra từ Token để biết ai đang gọi API
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    // Hàm trích xuất Role ra từ Token để phục vụ phân quyền
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class); // Lấy chính xác cái key "role" bạn đã lưu lúc generate
    }

    // Hàm kiểm tra xem Token còn hạn sử dụng hay không
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // Token sai, lỗi hoặc hết hạn
        }
    }
}