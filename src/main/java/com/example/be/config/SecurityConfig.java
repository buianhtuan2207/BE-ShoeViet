package com.example.be.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private com.example.be.util.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Các API auth (đăng ký, login, otp) thì cho phép vào tự do không cần token
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/brands/**").permitAll()
                        // 2. Các hành động POST, PUT, DELETE vào sản phẩm/danh mục/thương hiệu/user bắt buộc phải ĐĂNG NHẬP
                        .requestMatchers("/api/users/**", "/api/products/**", "/api/categories/**", "/api/brands/**").authenticated()
                        // Các request khác còn lại cũng bắt đăng nhập
                        .anyRequest().authenticated()
                )
                // Giữ nguyên filter cũ của bạn bên dưới
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}