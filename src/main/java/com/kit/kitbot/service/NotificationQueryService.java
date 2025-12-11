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


    public List<Notification> getMyNotifications(String userEmail) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userEmail);
    }


    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markRead();
            notificationRepository.save(notification);
        });
    }


    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByUserIdAndReadFalse(userEmail);
    }

    public void deleteNotification(String notificationId, String userEmail) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {

            if (notification.getUserId().equals(userEmail)) {
                notificationRepository.delete(notification);
            }
        });
    }


    public void deleteAllMyNotifications(String userEmail) {
        notificationRepository.deleteByUserId(userEmail);
    }
}