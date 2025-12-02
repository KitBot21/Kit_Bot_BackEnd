package com.kit.kitbot.service;

import com.kit.kitbot.document.User;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    // Key: schoolEmail, Value: {code, googleEmail}
    private final Map<String, VerificationData> verificationCodes = new ConcurrentHashMap<>();

    private record VerificationData(String code, String googleEmail) {}

    // 1. 인증 메일 발송
    public void sendVerificationEmail(String studentId, String googleEmail) {
        String schoolEmail = studentId + "@kumoh.ac.kr";

        // 이미 다른 계정에서 인증된 학교 메일인지 체크
        if (userRepository.existsBySchoolEmail(schoolEmail)) {
            throw new IllegalStateException("이미 다른 계정에서 인증된 학교 메일입니다.");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));

        verificationCodes.put(schoolEmail, new VerificationData(code, googleEmail));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(schoolEmail);
        message.setSubject("[KIT-Bot] 금오공대 학생 인증 번호입니다.");
        message.setText("인증 번호: " + code + "\n\n앱으로 돌아가서 인증번호를 입력해주세요.");

        mailSender.send(message);
        System.out.println("✅ 메일 발송 성성공: " + schoolEmail + " -> " + code);
    }

    // 2. 인증 번호 검증 & 등급업(kumoh)
    @Transactional
    public User verifyCode(String studentId, String code, String googleEmail) {
        String schoolEmail = studentId + "@kumoh.ac.kr";
        VerificationData data = verificationCodes.get(schoolEmail);

        if (data != null && data.code().equals(code) && data.googleEmail().equals(googleEmail)) {
            verificationCodes.remove(schoolEmail);

            User user = userRepository.findByGoogleEmail(googleEmail)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            user.setRole(User.Role.kumoh);
            user.setSchoolEmail(schoolEmail);
            userRepository.save(user);

            System.out.println("🎉 인증 성공! 등급 변경 완료: " + user.getUsername());

            return user;
        }
        return null;
    }
}