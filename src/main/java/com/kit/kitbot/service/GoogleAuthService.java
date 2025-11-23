package com.kit.kitbot.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kit.kitbot.document.User;
import com.kit.kitbot.dto.AuthResponse;
import com.kit.kitbot.dto.user.UserDto;
import com.kit.kitbot.repository.User.UserRepository;
import com.kit.kitbot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${google.client-id.android}")
    private String androidClientId;

    @Value("${google.client-id.ios}")
    private String iosClientId;

    public AuthResponse loginWithGoogle(String idToken) {
        try {
            // 1단계: Google ID 토큰 검증
            log.info("Google ID 토큰 검증 시작");
            GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

            // 2단계: 토큰에서 사용자 정보 추출
            String googleId = payload.getSubject();         // Google 고유 ID
            String email = payload.getEmail();              // 이메일
            String name = (String) payload.get("name");     // 이름
            String picture = (String) payload.get("picture"); // 프로필 사진

            log.info("Google 사용자 정보: email={}, name={}", email, name);

            // 3단계: DB에서 사용자 찾기 or 생성
            User user = userRepository.findByGoogleEmail(email)
                    .orElseGet(() -> {
                        log.info("신규 사용자 생성: {}", email);
                        return createNewUser(googleId, email, name, picture);
                    });

            // 4단계: 우리 서비스의 JWT 토큰 생성
            String accessToken = jwtTokenProvider.createToken(user.getId(), user.getGoogleEmail());

            log.info("로그인 성공: userId={}", user.getId());

            // 5단계: 응답 반환
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .user(UserDto.from(user))
                    .build();

        } catch (Exception e) {
            log.error("Google 로그인 실패", e);
            throw new RuntimeException("Google 로그인 실패: " + e.getMessage(), e);
        }
    }

    // Google ID 토큰이 진짜인지 검증
    private GoogleIdToken.Payload verifyGoogleToken(String idToken) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Arrays.asList(androidClientId, iosClientId))
                .build();

        GoogleIdToken googleIdToken = verifier.verify(idToken);

        if (googleIdToken == null) {
            throw new RuntimeException("유효하지 않은 Google ID 토큰입니다.");
        }

        return googleIdToken.getPayload();
    }

    // 새 사용자 생성 후 DB 저장
    private User createNewUser(String googleId, String email, String name, String picture) {
        User user = User.fromGoogleOAuth(googleId, email, name, picture);
        return userRepository.save(user);
    }
}