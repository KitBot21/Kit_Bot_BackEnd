package com.kit.kitbot.document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

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
    private String googleEmail;     // 구글 이메일 (UNIQUE)

    private String googleId;        // 구글 OIDC id
    private String schoolEmail;     // 학교 이메일
    private String username;        // 닉네임
    private Role role;              // guest, kumoh, admin
    private Status status;          // active, blocked, deleted
    private Instant createdAt;      // 생성 시각
    private String locale;          // 언어 (ko_KR)
    private String profileImg;      // 프로필 이미지 경로

    public enum Role { guest, kumoh, admin }
    public enum Status { active, blocked, deleted }

    public static User ofDefault() {
        return User.builder()
                .role(Role.guest)
                .status(Status.active)
                .createdAt(Instant.now())
                .locale("ko_KR")
                .profileImg("/static/images/default_profile.png")
                .build();
    }
}
