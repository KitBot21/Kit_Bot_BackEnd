package com.kit.kitbot.service;

import com.kit.kitbot.document.Notification;
import com.kit.kitbot.repository.Notice.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    // 내 알림 목록 (최신순)
    public List<Notification> getMyNotifications(String userEmail) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userEmail);
    }

    // 읽음 처리
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markRead();
            notificationRepository.save(notification);
        });
    }

    // 안 읽은 개수
    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByUserIdAndReadFalse(userEmail);
    }
}