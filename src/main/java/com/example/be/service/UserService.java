package com.example.be.service;

import com.example.be.dto.req.LoginRequest;
import com.example.be.dto.req.RegisterRequest;
import com.example.be.dto.res.LoginResponse;
import com.example.be.entity.OtpVerification;
import com.example.be.entity.User;
import com.example.be.repository.OtpVerificationRepository;
import com.example.be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.example.be.security.JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email này đã được đăng ký sử dụng!";
        }

        // 1. Tạo và Lưu User
        User user = new User();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        if (request.getRole() != null && request.getRole().equalsIgnoreCase("admin")) {
            user.setRole("admin");
        } else {
            user.setRole("customer");
        }
        User savedUser = userRepository.save(user);

        // 2. TẠO MÃ OTP 6 SỐ
        String otp6So = String.format("%06d", new Random().nextInt(999999));

        OtpVerification otp = new OtpVerification();
        otp.setUser(savedUser);
        otp.setOtpCode(otp6So); // Lưu vào cột otp_code
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        otpVerificationRepository.save(otp);

        // 3. Gửi email chứa 6 số
        try {
            emailService.sendOtpEmail(savedUser.getEmail(), otp6So);
        } catch (Exception e) {
            e.printStackTrace();
            return "Đăng ký thành công nhưng lỗi khi gửi email xác thực!";
        }

        return "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP 6 số.";
    }

    // Cập nhật lại hàm verify để nhận 6 số
    public boolean verifyOtp(String otpCode) {
        Optional<OtpVerification> optionalOtp = otpVerificationRepository.findByOtpCode(otpCode);

        if (optionalOtp.isEmpty()) {
            return false; // Sai mã OTP
        }

        OtpVerification otp = optionalOtp.get();

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpVerificationRepository.delete(otp);
            return false; // OTP hết hạn
        }

        User user = otp.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        otpVerificationRepository.delete(otp); // Xóa OTP sau khi dùng
        return true;
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Kiểm tra Email xem có tồn tại không
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không chính xác!"));

        // 2. Kiểm tra mật khẩu (Giải mã và so khớp với password_hash trong DB)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        }

        // 3. Kiểm tra xem tài khoản đã được kích hoạt OTP chưa
        if (!user.isEnabled()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt OTP! Vui lòng xác thực trước.");
        }

        // 4. Tạo token thành công và trả về cho người dùng
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole()); // Gọi qua object đã tiêm
        return new LoginResponse(token, user.getEmail(), user.getFullName(), user.getRole());
    }
}