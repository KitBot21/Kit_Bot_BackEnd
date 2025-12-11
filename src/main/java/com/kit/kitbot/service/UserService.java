package com.kit.kitbot.service;


import com.kit.kitbot.document.User;
import com.kit.kitbot.dto.user.SetUsernameRequest;
import com.kit.kitbot.dto.user.SetUsernameResponse;
import com.kit.kitbot.dto.user.UserDto;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public SetUsernameResponse setUsername(String userId, SetUsernameRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String newNickName = request.getUsername();

        if(newNickName != user.getUsername()){

        }

        if(!newNickName.equals(user.getUsername()) && userRepository.existsByUsername(newNickName)){
            return new SetUsernameResponse(false,"이미 사용중인 닉네임입니다.", null);
        }


        user.setUsername(newNickName);
        User savedUser = userRepository.save(user);

        log.info("닉네임 설정 완료: userId={}, username={}", userId, request.getUsername());

        return new SetUsernameResponse(
                true,
                "닉네임이 설정되었습니다.",
                UserDto.from(savedUser)
        );
    }


    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public void updatePushToken(String userId, String pushToken) {

        if (pushToken != null) {
            List<User> usersWithSameToken = userRepository.findAllByPushToken(pushToken);
            for (User u : usersWithSameToken) {
                if (!u.getId().equals(userId)) {
                    u.setPushToken(null);
                    userRepository.save(u);
                    log.info("중복 푸시 토큰 제거: userId={}", u.getId());
                }
            }
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setPushToken(pushToken);
        userRepository.save(user);
        log.info("푸시 토큰 업데이트 완료: userId={}", userId);
    }
    @Transactional
    public void withdraw(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));


        if (user.getStatus() == User.Status.deleted) {
            throw new RuntimeException("이미 탈퇴한 사용자입니다.");
        }


        user.setStatus(User.Status.deleted);
        user.setDeletedAt(Instant.now());
        user.setPushToken(null);

        userRepository.save(user);
        log.info("회원 탈퇴 처리 완료: userId={}", userId);
    }

    public void updateNotificationEnabled(String userId, Boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setNotificationEnabled(enabled);
        userRepository.save(user);
        log.info("알림 설정 변경: userId={}, enabled={}", userId, enabled);
    }

    public void deletePushToken(String email) {
        User user = userRepository.findByGoogleEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPushToken(null);
        userRepository.save(user);
    }

}