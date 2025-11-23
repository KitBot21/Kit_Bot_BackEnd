package com.kit.kitbot.dto;

import com.kit.kitbot.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;  // 우리 서비스의 JWT 토큰
    private UserDto user;        // 사용자 정보
}