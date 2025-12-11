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
@NoArgsConstructor
@Builder
public class UserDto {
    private String id;
    private String email;
    private String username;
    private String role;
    private boolean usernameSet;
    private boolean notificationEnabled;

    private String pushToken;
    private List<String> keywords;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getGoogleEmail())
                .username(user.getUsername())
                .role(user.getRole().toString())
                .usernameSet(user.hasUsername())

                .pushToken(user.getPushToken())
                .notificationEnabled(user.getNotificationEnabled() != null ? user.getNotificationEnabled() : true)
                .keywords(user.getKeywords() != null ? user.getKeywords() : new ArrayList<>())
                .build();
    }
}