package com.example.be.service;

import com.example.be.dto.req.*;
import com.example.be.dto.res.LoginResponse;
import com.example.be.entity.OtpVerification;
import com.example.be.entity.User;
import com.example.be.repository.OtpVerificationRepository;
import com.example.be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    private com.example.be.util.JwtUtils jwtUtils;

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
    public String processForgotPassword(ForgotPasswordRequest request) {
        // 1. Kiểm tra xem email có tồn tại trong hệ thống không
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email này không tồn tại trên hệ thống!"));

        // 2. Tạo mã OTP quên mật khẩu (6 số)
        String otp6So = String.format("%06d", new Random().nextInt(999999));

        // 3. Lưu vào bảng otp_verification (Xóa OTP cũ của user này nếu có để tránh rác DB)
        // Bạn có thể viết thêm hàm deleteByUserId trong Repo hoặc cứ lưu đè một dòng mới
        OtpVerification otp = new OtpVerification();
        otp.setUser(user);
        otp.setOtpCode(otp6So);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(10)); // OTP quên mật khẩu cho hết hạn sau 10 phút
        otpVerificationRepository.save(otp);

        // 4. Gửi mail chứa OTP cho khách
        try {
            emailService.sendOtpEmail(user.getEmail(), otp6So); // Tái sử dụng hàm gửi mail cũ của bạn
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi gửi Email!");
        }

        return "Mã OTP đặt lại mật khẩu đã được gửi vào Email của bạn.";
    }

    public String verifyForgotOtp(VerifyForgotOtpRequest request) {
        // Tìm OTP dựa trên otpCode
        OtpVerification otp = otpVerificationRepository.findByOtpCode(request.getOtp())
                .orElseThrow(() -> new RuntimeException("Mã OTP không chính xác!"));

        // Kiểm tra xem OTP này có phải của email đang yêu cầu không
        if (!otp.getUser().getEmail().equals(request.getEmail())) {
            throw new RuntimeException("Mã OTP không trùng khớp với tài khoản này!");
        }

        // Kiểm tra hết hạn
        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpVerificationRepository.delete(otp);
            throw new RuntimeException("Mã OTP đã hết hạn!");
        }

        return "Mã OTP hợp lệ! Bạn có thể tiến hành đổi mật khẩu.";
    }

    public String resetPassword(ResetPasswordRequest request) {
        // Xác thực lại OTP một lần nữa cho chắc chắn trước khi đổi mật khẩu
        OtpVerification otp = otpVerificationRepository.findByOtpCode(request.getOtp())
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ hoặc đã bị hủy!"));

        if (!otp.getUser().getEmail().equals(request.getEmail())) {
            throw new RuntimeException("Yêu cầu không hợp lệ!");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpVerificationRepository.delete(otp);
            throw new RuntimeException("Mã OTP đã hết hạn!");
        }

        // Tiến hành đổi mật khẩu
        User user = otp.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword())); // Mã hóa mật khẩu mới
        userRepository.save(user);

        // Xóa mã OTP đi sau khi đã đổi mật khẩu thành công
        otpVerificationRepository.delete(otp);

        return "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.";
    }
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    // Lấy tất cả danh sách người dùng trong hệ thống
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Cập nhật thông tin hồ sơ
    public User updateProfileByEmail(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Chỉ cập nhật fullName nếu request có truyền lên dữ liệu mới
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // Chỉ cập nhật phone nếu request có truyền lên dữ liệu mới
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        // Chỉ cập nhật address nếu request có truyền lên dữ liệu mới
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            user.setAddress(request.getAddress());
        }

        return userRepository.save(user);
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
    }
}