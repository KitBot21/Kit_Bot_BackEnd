package com.kit.kitbot.service;

import com.kit.kitbot.document.NoticeKeyword;
import com.kit.kitbot.document.NoticeKeywordSubscription;
import com.kit.kitbot.document.Notification;
import com.kit.kitbot.dto.crawl.CrawlerRequestDTO;
import com.kit.kitbot.repository.Notice.NoticeKeywordSubscriptionRepository;
import com.kit.kitbot.repository.Notice.NotificationRepository;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeKeywordNotifyService {

    private final NoticeKeywordSubscriptionRepository subRepo;
    private final NotificationRepository notificationRepo;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void notifySubscribers(CrawlerRequestDTO request) {
        try {
            NoticeKeyword keywordEnum = NoticeKeyword.valueOf(request.getKeyword().toUpperCase());

            List<NoticeKeywordSubscription> subs =
                    subRepo.findByKeywordAndEnabledTrue(keywordEnum);

            if (subs.isEmpty()) {
                log.info("'{}' 키워드 구독자가 없어 알림을 건너뜁니다.", keywordEnum);
                return;
            }

            int count = 0;
            for (NoticeKeywordSubscription sub : subs) {
                String userId = sub.getUserId();

                userRepository.findByGoogleEmail(userId).ifPresentOrElse(
                        user -> {
                            String pushToken = user.getPushToken();
                            log.info("유저 찾음! email: {}, pushToken: {}", userId, pushToken);

                            if (pushToken != null && !pushToken.isEmpty()) {
                                boolean sent = notificationService.sendPush(
                                        pushToken,
                                        "🔔 [" + keywordEnum.name() + "] 새 공지 알림",
                                        request.getTitle(),
                                        request.getUrl()
                                );

                                if (sent) {
                                    notificationRepo.save(Notification.builder()
                                            .userId(userId)
                                            .type("NOTICE_KEYWORD")
                                            .keyword(keywordEnum.name())
                                            .noticeId(request.getUrl())
                                            .title(request.getTitle())
                                            .pushed(true)
                                            .read(false)
                                            .build());
                                }
                            } else {
                                log.warn("유저 pushToken이 없음: {}", userId);
                            }
                        },
                        () -> {
                            log.warn("유저를 찾을 수 없음: {}", userId);
                        }
                );
                count++;
            }
            log.info("총 {}명에게 '{}' 알림 발송 완료", count, keywordEnum);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 키워드가 수신되었습니다: {}", request.getKeyword());
        } catch (Exception e) {
            log.error("알림 발송 중 에러 발생", e);
        }
    }
}