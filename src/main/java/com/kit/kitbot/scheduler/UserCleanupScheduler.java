package com.kit.kitbot.scheduler;

import com.kit.kitbot.document.User;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // 매일 새벽 3시에 실행 (서버 부하 적은 시간)
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupDeletedUsers() {
        log.info("=== 탈퇴 유저 정리 작업 시작 ===");

        // 30일 전 시간 계산
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        // 30일 이상 지난 탈퇴 유저 조회
        List<User> usersToDelete = userRepository
                .findByStatusAndDeletedAtBefore(User.Status.deleted, thirtyDaysAgo);

        if (usersToDelete.isEmpty()) {
            log.info("삭제할 유저 없음");
            return;
        }

        log.info("삭제 대상 유저 수: {}", usersToDelete.size());

        for (User user : usersToDelete) {
            try {
                // Hard Delete
                userRepository.delete(user);
                log.info("유저 삭제 완료: userId={}, email={}", user.getId(), user.getGoogleEmail());
            } catch (Exception e) {
                log.error("유저 삭제 실패: userId={}", user.getId(), e);
            }
        }

        log.info("=== 탈퇴 유저 정리 작업 완료 ===");
    }
}