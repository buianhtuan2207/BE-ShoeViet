package com.example.be.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
                // 1. KÍCH HOẠT CORS: Để Spring Security đi xuyên qua và áp dụng cấu hình từ file WebConfig của bạn
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các request OPTIONS (Preflight) đi qua không cần kiểm tra token
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. CÔNG KHAI (Không cần đăng nhập): Cho phép xem (GET) sản phẩm, danh mục, thương hiệu
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").permitAll()

                        // 2. CHỈ ADMIN MỚI ĐƯỢC LÀM (Tạo mới, Sửa, Xóa)
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")

                        // 3. YÊU CẦU ĐĂNG NHẬP THÔNG THƯỜNG (User & Admin đều vào được)
                        .requestMatchers("/api/users/**").authenticated()

                        // Tất cả các request còn lại chưa cấu hình ở trên thì bắt buộc phải ĐĂNG NHẬP
                        .anyRequest().authenticated()
                )
                // Giữ nguyên filter JWT của bạn
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}