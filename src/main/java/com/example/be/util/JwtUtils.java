package com.example.be.security;

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
}