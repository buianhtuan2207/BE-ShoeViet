package com.example.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt cấu hình CSRF để Postman có thể gọi các API POST, PUT, DELETE mượt mà
                .csrf(csrf -> csrf.disable())

                // 2. Cấu hình phân quyền đường dẫn
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người truy cập vào các API bắt đầu bằng /api/auth/ mà không cần đăng nhập
                        .requestMatchers("/api/auth/**").permitAll()

                        // Tất cả các yêu cầu khác ngoài /api/auth/** đều bắt buộc phải đăng nhập/xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}