package com.kit.kitbot.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;  // 앱에서 받은 Google ID 토큰
}