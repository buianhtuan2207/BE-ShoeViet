package com.example.be.dto.req;

public class VerifyForgotOtpRequest {
    private String email;
    private String otp;

    // Getters và Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}