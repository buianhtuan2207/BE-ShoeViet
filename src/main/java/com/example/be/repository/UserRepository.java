package com.example.be.repository;

import com.example.be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Kiểm tra xem email đã tồn tại trong DB chưa
    boolean existsByEmail(String email);

    // Tìm user bằng email (dùng cho đăng nhập sau này)
    Optional<User> findByEmail(String email);
}
