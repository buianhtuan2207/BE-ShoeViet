package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data // Tự động tạo getter, setter, toString, equals, hashCode nhờ Lombok
@NoArgsConstructor // Tạo constructor không tham số bắt buộc của JPA
@AllArgsConstructor // Tạo constructor đầy đủ tham số
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // Mật khẩu đã được mã hóa BCrypt

    @Column(length = 15)
    private String phone;

    private String address;

    @Column(name = "otp_code", length = 6)
    private String otpCode; // Lưu mã OTP 6 chữ số khi yêu cầu quên mật khẩu

    @Column(name = "otp_requested_time")
    private LocalDateTime otpRequestedTime; // Lưu thời gian tạo OTP để tính thời gian hết hạn (ví dụ: sau 60s)
}