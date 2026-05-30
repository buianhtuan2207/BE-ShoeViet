package com.example.be.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String phone;
    private String email;
    private String password;
    private String role;
}