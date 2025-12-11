package com.kit.kitbot.controller;

import com.kit.kitbot.document.User;
import com.kit.kitbot.service.EmailService;
import com.kit.kitbot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String googleEmail = body.get("googleEmail");

        if (studentId == null || googleEmail == null) {
            return ResponseEntity.badRequest().body("학번과 구글 이메일이 필요합니다.");
        }

        try {
            emailService.sendVerificationEmail(studentId, googleEmail);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String code = body.get("code");
        String googleEmail = body.get("googleEmail");

        User user = emailService.verifyCode(studentId, code, googleEmail);

        if (user != null) {
            String newToken = jwtTokenProvider.createToken(
                    user.getId(),
                    user.getGoogleEmail(),
                    user.getRole().name()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newToken);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getGoogleEmail());
            userData.put("username", user.getUsername());
            userData.put("profileImg", user.getProfileImg());
            userData.put("role", user.getRole().name());
            userData.put("usernameSet", user.hasUsername());
            userData.put("schoolEmail", user.getSchoolEmail());

            response.put("user", userData);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body("인증번호가 일치하지 않습니다.");
        }
    }
}