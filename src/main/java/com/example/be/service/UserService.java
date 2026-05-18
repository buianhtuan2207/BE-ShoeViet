package com.example.be.service;

import com.example.be.dto.RegisterRequest;
import com.example.be.entity.User;
import com.example.be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(RegisterRequest request) {
        // 1. Kiểm tra email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email này đã được đăng ký sử dụng!";
        }

        // 2. Tạo đối tượng User mới và set các thông tin
        User user = new User();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());

        // Mã hóa mật khẩu
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        // 3. Lưu vào database
        userRepository.save(user);

        return "Đăng ký tài khoản thành công!";
    }
}