package com.kit.kitbot.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SetUsernameResponse {
    private boolean success;
    private String message;
    private UserDto user;
}