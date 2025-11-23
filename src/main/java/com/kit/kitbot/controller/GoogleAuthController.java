package com.kit.kitbot.controller;

import com.kit.kitbot.dto.AuthResponse;
import com.kit.kitbot.dto.GoogleLoginRequest;
import com.kit.kitbot.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        log.info("Google 로그인 요청 수신");
        AuthResponse response = googleAuthService.loginWithGoogle(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}