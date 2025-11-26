package com.kit.kitbot.controller;

import com.kit.kitbot.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // 1. 인증번호 전송 API
    // POST /api/auth/email/send
    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String googleEmail = body.get("googleEmail"); // 현재 로그인한 유저 찾기용

        if (studentId == null || googleEmail == null) {
            return ResponseEntity.badRequest().body("학번과 구글 이메일이 필요합니다.");
        }

        emailService.sendVerificationEmail(studentId, googleEmail);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    // 2. 인증번호 검증 API
    // POST /api/auth/email/verify
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String code = body.get("code");
        String googleEmail = body.get("googleEmail");

        boolean isVerified = emailService.verifyCode(studentId, code, googleEmail);

        if (isVerified) {
            return ResponseEntity.ok("인증 성공! 이제 글을 작성할 수 있습니다.");
        } else {
            return ResponseEntity.status(400).body("인증번호가 일치하지 않습니다.");
        }
    }
}