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
import java.util.Collections;
import java.util.Arrays;


@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {


    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${google.client-id.android}")
    private String androidClientId;
    private final String WEB_CLIENT_ID = "358721642016-j5hcv6tjn6rvu04hk65qokap8hulhlgv.apps.googleusercontent.com";



    public AuthResponse loginWithGoogle(String idToken) {
        try {

            log.info("Google ID 토큰 검증 시작");
            GoogleIdToken.Payload payload = verifyGoogleToken(idToken);


            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            log.info("Google 사용자 정보: email={}, name={}", email, name);


            User user = userRepository.findByGoogleEmail(email)
                    .map(existingUser -> {

                        if (existingUser.getStatus() == User.Status.deleted) {
                            log.info("탈퇴 사용자 복구: {}", email);
                            existingUser.setStatus(User.Status.active);
                            existingUser.setDeletedAt(null);
                            return userRepository.save(existingUser);
                        }
                        return existingUser;
                    })
                    .orElseGet(() -> {
                        log.info("신규 사용자 생성: {}", email);
                        return createNewUser(googleId, email, name, picture);
                    });

            String accessToken = jwtTokenProvider.createToken(user.getId(), user.getGoogleEmail(), user.getRole().name());

            log.info("로그인 성공: userId={}", user.getId());


            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .user(UserDto.from(user))
                    .build();

        } catch (Exception e) {
            log.error("Google 로그인 실패", e)   ;
            throw new RuntimeException("Google 로그인 실패: " + e.getMessage(), e);
        }
    }


    private GoogleIdToken.Payload verifyGoogleToken(String idToken) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Arrays.asList(androidClientId,  WEB_CLIENT_ID))
                .build();

        GoogleIdToken googleIdToken = verifier.verify(idToken);

        if (googleIdToken == null) {
            throw new RuntimeException("유효하지 않은 Google ID 토큰입니다.");
        }

        return googleIdToken.getPayload();
    }


    private User createNewUser(String googleId, String email, String name, String picture) {
        User user = User.fromGoogleOAuth(googleId, email, name, picture);
        return userRepository.save(user);
    }
}