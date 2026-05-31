package com.example.be.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        // 1. CÔNG KHAI (Không cần đăng nhập): Cho phép xem (GET) sản phẩm, danh mục, thương hiệu
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").permitAll()

                        // 2. CHỈ ADMIN MỚI ĐƯỢC LÀM (Tạo mới, Sửa, Xóa)
                        // Giới hạn các phương thức POST, PUT, DELETE của Sản phẩm/Danh mục/Thương hiệu/Biến thể cho vai trò ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")

                        // 3. YÊU CẦU ĐĂNG NHẬP THÔNG THƯỜNG (User & Admin đều vào được)
                        // Ví dụ: Xem/Sửa thông tin cá nhân của User, Đặt hàng, Xem lịch sử đơn hàng...
                        .requestMatchers("/api/users/**").authenticated()

                        // Tất cả các request còn lại chưa cấu hình ở trên thì bắt buộc phải ĐĂNG NHẬP
                        .anyRequest().authenticated()
                )
                // Giữ nguyên filter JWT cũ của bạn
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}