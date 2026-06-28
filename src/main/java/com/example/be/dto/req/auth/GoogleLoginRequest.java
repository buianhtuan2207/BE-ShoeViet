package com.example.be.dto.req.auth;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}
