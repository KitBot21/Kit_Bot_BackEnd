package com.kit.kitbot.controller.NoticeControllerManage;

import com.kit.kitbot.document.Notification;
import com.kit.kitbot.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    // 1. 내 알림 목록 조회
    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        List<Notification> notifications = notificationQueryService.getMyNotifications(userEmail);
        return ResponseEntity.ok(notifications);
    }

    // 2. 알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationQueryService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // 3. 안 읽은 알림 개수
    @GetMapping("/me/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        long count = notificationQueryService.getUnreadCount(userEmail);
        return ResponseEntity.ok(count);
    }
}