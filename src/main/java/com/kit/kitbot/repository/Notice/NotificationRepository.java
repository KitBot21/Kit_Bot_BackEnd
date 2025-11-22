package com.kit.kitbot.repository.Notice;

import com.kit.kitbot.document.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository
        extends MongoRepository<Notification, String> {

    boolean existsByUserIdAndNoticeIdAndKeyword(String userId, String noticeId, String keyword);

}