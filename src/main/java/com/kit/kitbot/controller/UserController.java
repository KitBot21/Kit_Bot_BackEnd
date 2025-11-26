package com.kit.kitbot.controller;

import com.kit.kitbot.dto.user.SetUsernameRequest;
import com.kit.kitbot.dto.user.SetUsernameResponse;
import com.kit.kitbot.security.JwtTokenProvider;
import com.kit.kitbot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 닉네임 설정
    @PostMapping("/username")
    public ResponseEntity<SetUsernameResponse> setUsername(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody SetUsernameRequest request
    ) {
        // JWT 토큰에서 userId 추출
        String userId = getUserIdFromToken(token);

        SetUsernameResponse response = userService.setUsername(userId, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // 닉네임 중복 체크
    @GetMapping("/username/check")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
            @RequestParam String username
    ) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // 토큰에서 userId 추출 헬퍼 메서드
    private String getUserIdFromToken(String token) {
        String actualToken = token.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(actualToken);
    }

    @PostMapping("/push-token")
    public ResponseEntity<String> updatePushToken(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> requestBody
    ) {
        // 1. 토큰에서 유저 ID 추출 (기존 헬퍼 메서드 재사용)
        String userId = getUserIdFromToken(token);

        // 2. Body에서 푸시 토큰 꺼내기
        String pushToken = requestBody.get("pushToken");

        if (pushToken == null || pushToken.isEmpty()) {
            return ResponseEntity.badRequest().body("토큰이 없습니다.");
        }

        // 3. 서비스 호출
        userService.updatePushToken(userId, pushToken);

        return ResponseEntity.ok("푸시 토큰이 저장되었습니다.");
    }
}