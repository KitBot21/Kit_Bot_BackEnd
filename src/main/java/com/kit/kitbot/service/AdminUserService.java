package com.kit.kitbot.service;

import com.kit.kitbot.document.User;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<User> searchUsers(String keyword, String role, String status, Pageable pageable) {

        return userRepository.findAll(pageable);
    }

    public void blockUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        user.block();
        userRepository.save(user);
    }

    public void activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        user.activate();
        userRepository.save(user);
    }
}
