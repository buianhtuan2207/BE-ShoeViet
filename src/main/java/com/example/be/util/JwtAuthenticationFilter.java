package com.example.be.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 👈 Thêm import này
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List; // 👈 Thêm import này

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lấy chuỗi Authorization từ Header
        String authHeader = request.getHeader("Authorization");

        // 2. Kiểm tra xem có đúng định dạng "Bearer <Token>" không
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Cắt bỏ chữ "Bearer " lấy phần Token phía sau

            // 3. Xác thực Token
            if (jwtUtils.validateToken(token)) {
                String email = jwtUtils.getEmailFromToken(token);

                //  Lấy role từ token ra
                String role = jwtUtils.getRoleFromToken(token);

                // 4. Chuyển chuỗi role (ví dụ: "admin") thành quyền hạn hợp lệ của Spring Security
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                // Nạp danh sách authorities thay vì Collections.emptyList()
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Cho phép request tiếp tục đi tiếp vào Controller
        filterChain.doFilter(request, response);
    }
}