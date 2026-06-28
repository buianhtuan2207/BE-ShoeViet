package com.example.be.service.auth;

import com.example.be.dto.req.auth.GoogleLoginRequest;
import com.example.be.entity.User;
import com.example.be.repository.UserRepository;
import com.example.be.util.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Map;

@Service
public class AuthService {

    @Value("${google.client-id}")
    private String googleClientId;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

    public Object loginWithGoogle(GoogleLoginRequest request) {
        try {
            // 1. Khởi tạo bộ xác thực của Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            // 2. Kiểm tra tính hợp lệ của Token
            GoogleIdToken idToken = verifier.verify(request.getIdToken());

            if (idToken != null) {
                // 3. Giải mã lấy thông tin
                Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String fullName = (String) payload.get("name");
                String avatarUrl = (String) payload.get("picture");

                // 4. Kiểm tra Database và tự động Đăng ký nếu chưa có
                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setRole("customer"); // Mặc định là khách hàng

                    // Nếu bảng User của bạn có cột avatar và bạn muốn lưu:
                    // newUser.setLogoUrl(avatarUrl);

                    return userRepository.save(newUser);
                });

                // 5. Sinh JWT Token bằng hàm bạn đã viết (truyền email và role)
                String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole());

                // 6. Trả về đúng định dạng Frontend mong đợi
                return Map.of(
                        "token", accessToken,
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "role", user.getRole()
                );

            } else {
                throw new RuntimeException("Google ID Token không hợp lệ hoặc đã hết hạn!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xác thực với Google: " + e.getMessage());
        }
    }
}