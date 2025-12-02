package com.kit.kitbot.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final String userId;  // 👈 MongoDB ObjectId

    public CustomUserDetails(String email, String userId, Collection<? extends GrantedAuthority> authorities) {
        super(email, "", authorities);
        this.userId = userId;
    }
}