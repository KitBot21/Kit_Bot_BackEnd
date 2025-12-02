package com.kit.kitbot.dto.user;

import com.kit.kitbot.document.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor // JSON 파싱 등을 위해 기본 생성자 추가 권장
@Builder
public class UserDto {
    private String id;
    private String email;
    private String username;
    private String role;
    private boolean usernameSet;
    private boolean notificationEnabled;

    // 👇 [추가] 앱 설정 상태 확인용
    private String pushToken;       // 푸시 토큰 (등록 여부 확인용)
    private List<String> keywords;  // 구독 중인 키워드 목록

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getGoogleEmail())
                .username(user.getUsername())
                .role(user.getRole().toString())
                .usernameSet(user.hasUsername())

                // 👇 [추가] 엔티티에서 값 가져오기
                .pushToken(user.getPushToken())
                // 키워드 리스트가 null이면 빈 배열 []로 보내기 (프론트 에러 방지)
                .notificationEnabled(user.getNotificationEnabled() != null ? user.getNotificationEnabled() : true)
                .keywords(user.getKeywords() != null ? user.getKeywords() : new ArrayList<>())
                .build();
    }
}