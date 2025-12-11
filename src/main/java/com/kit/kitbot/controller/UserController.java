package com.kit.kitbot.controller;

import com.kit.kitbot.dto.user.SetUsernameRequest;
import com.kit.kitbot.dto.user.SetUsernameResponse;
import com.kit.kitbot.security.JwtTokenProvider;
import com.kit.kitbot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/username")
    public ResponseEntity<SetUsernameResponse> setUsername(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody SetUsernameRequest request
    ) {
        String userId = getUserIdFromToken(token);

        SetUsernameResponse response = userService.setUsername(userId, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/check")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
            @RequestParam String username
    ) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    private String getUserIdFromToken(String token) {
        String actualToken = token.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(actualToken);
    }

    @PostMapping("/push-token")
    public ResponseEntity<String> updatePushToken(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> requestBody
    ) {
        String userId = getUserIdFromToken(token);

        String pushToken = requestBody.get("pushToken");

        if (pushToken == null || pushToken.isEmpty()) {
            return ResponseEntity.badRequest().body("토큰이 없습니다.");
        }

        userService.updatePushToken(userId, pushToken);

        return ResponseEntity.ok("푸시 토큰이 저장되었습니다.");
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> withdraw(
            @RequestHeader("Authorization") String token
    ) {
        String userId = getUserIdFromToken(token);
        userService.withdraw(userId);
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    @PatchMapping("/notification")
    public ResponseEntity<Map<String, Object>> updateNotificationSetting(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Boolean> requestBody
    ) {
        String userId = getUserIdFromToken(token);
        Boolean enabled = requestBody.get("enabled");

        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "enabled 값이 필요합니다."));
        }

        userService.updateNotificationEnabled(userId, enabled);

        return ResponseEntity.ok(Map.of("success", true, "notificationEnabled", enabled));
    }

    @DeleteMapping("/api/user/push-token")
    public ResponseEntity<?> deletePushToken(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deletePushToken(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

}