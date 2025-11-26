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

    // 인증번호를 잠시 저장해두는 곳 (Key: 학교이메일, Value: 인증번호)
    // 서버 껐다 켜면 날아가지만, 지금 단계에선 충분합니다. (나중엔 Redis 사용 권장)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 1. 인증 메일 발송
    public void sendVerificationEmail(String studentId, String googleEmail) {
        String schoolEmail = studentId + "@kumoh.ac.kr";

        // 6자리 랜덤 숫자 생성
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 저장소에 저장 (나중에 검사를 위해)
        verificationCodes.put(schoolEmail, code);

        // 메일 전송 객체 생성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(schoolEmail);
        message.setSubject("[KIT-Bot] 금오공대 학생 인증 번호입니다.");
        message.setText("인증 번호: " + code + "\n\n앱으로 돌아가서 인증번호를 입력해주세요.");

        mailSender.send(message);
        System.out.println("✅ 메일 발송 성공: " + schoolEmail + " -> " + code);
    }

    // 2. 인증 번호 검증 & 등급업(kumoh)
    @Transactional
    public boolean verifyCode(String studentId, String code, String googleEmail) {
        String schoolEmail = studentId + "@kumoh.ac.kr";
        String savedCode = verificationCodes.get(schoolEmail);

        // 저장된 코드와 입력한 코드가 같은지 확인
        if (savedCode != null && savedCode.equals(code)) {
            // 인증 성공! -> 메모리에서 삭제 (재사용 방지)
            verificationCodes.remove(schoolEmail);

            // DB에서 유저를 찾아서 Role을 'kumoh'로 변경
            User user = userRepository.findByGoogleEmail(googleEmail)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            // 동료분 코드(User.java)의 setRole 사용
            user.setRole(User.Role.kumoh);
            user.setSchoolEmail(schoolEmail); // 학교 이메일 정보도 저장해주면 좋음

            userRepository.save(user); // 변경사항 저장
            System.out.println("🎉 인증 성공! 등급 변경 완료: " + user.getUsername());

            return true;
        }
        return false;
    }
}