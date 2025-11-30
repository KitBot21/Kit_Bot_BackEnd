package com.kit.kitbot.repository.Notice;

import com.kit.kitbot.document.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    boolean existsByUserIdAndNoticeIdAndKeyword(String userId, String noticeId, String keyword);

    // 👇 추가
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndReadFalse(String userId);
}