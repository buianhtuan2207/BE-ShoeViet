package com.example.be.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private com.example.be.util.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. KÍCH HOẠT CORS VÀ DÙNG BEAN CONFIG BÊN DƯỚI
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các request OPTIONS (Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. CÔNG KHAI (Không cần đăng nhập)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Cho phép FE gọi tự do vào API Shipping để lấy tỉnh/thành/phí ship
                        .requestMatchers("/api/v1/shipping/**", "/api/shipping/**", "/api/v1/payment/**", "/error").permitAll()

                        // Cho phép xem (GET) sản phẩm, danh mục, thương hiệu
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").permitAll()

                        // 2. CHỈ ADMIN MỚI ĐƯỢC LÀM (Tạo mới, Sửa, Xóa)
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/product-variants/**").hasAuthority("admin")

                        // 3. YÊU CẦU ĐĂNG NHẬP THÔNG THƯỜNG
                        .requestMatchers("/api/users/**").authenticated()

                        // Tất cả các request còn lại chưa cấu hình ở trên thì bắt buộc phải ĐĂNG NHẬP
                        .anyRequest().authenticated()
                )
                // Giữ nguyên filter JWT
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CẤU HÌNH CORS CHO TẦNG SECURITY
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép chính xác nguồn Frontend từ React
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Cho phép đầy đủ các phương thức gửi lên
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cho phép tất cả các Headers (bao gồm cả Authorization mang Token từ FE gửi lên)
        configuration.setAllowedHeaders(List.of("*"));

        // Cho phép đính kèm thông tin xác thực (Credentials) hợp lệ
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}