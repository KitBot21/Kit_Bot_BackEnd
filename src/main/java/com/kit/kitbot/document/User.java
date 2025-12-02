package com.kit.kitbot.document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String googleEmail;

    private String googleId;
    private String schoolEmail;
    private String username;        // null로 시작, 나중에 사용자가 설정
    private Role role;
    private Status status;
    private Instant createdAt;
    private String locale;
    private String profileImg;
    private String pushToken;
    private List<String> keywords;
    private Instant deletedAt;
    private Boolean notificationEnabled;  // 👈 추가

    public enum Role { guest, kumoh, admin }
    public enum Status { active, blocked, deleted }

    public static User ofDefault() {
        return User.builder()
                .role(Role.guest)
                .status(Status.active)
                .createdAt(Instant.now())
                .locale("ko_KR")
                .profileImg("/static/images/default_profile.png")
                .notificationEnabled(true)
                .build();
    }

    // Google OAuth 로그인용 - username은 null로 시작
    public static User fromGoogleOAuth(String googleId, String email, String name, String picture) {
        return User.builder()
                .googleId(googleId)
                .googleEmail(email)
                .username(null)  // 닉네임은 나중에 설정
                .profileImg(picture != null ? picture : "/static/images/default_profile.png")
                .role(Role.guest)
                .status(Status.active)
                .createdAt(Instant.now())
                .locale("ko_KR")
                .notificationEnabled(true)
                .build();
    }

    // 닉네임 설정 여부 확인
    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    public void block() {
        this.status = Status.blocked;
    }

    public void activate() {
        this.status = Status.active;
    }
}