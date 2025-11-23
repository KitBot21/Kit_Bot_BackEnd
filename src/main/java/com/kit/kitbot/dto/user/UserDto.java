package com.kit.kitbot.dto.user;

import com.kit.kitbot.document.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    private String id;
    private String email;
    private String username;
    private String role;
    private boolean isUsernameSet;  // 닉네임 설정 여부 (앱에서 화면 분기용)

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getGoogleEmail())
                .username(user.getUsername())
                .role(user.getRole().toString())
                .isUsernameSet(user.hasUsername())  // 닉네임 설정 여부
                .build();
    }
}