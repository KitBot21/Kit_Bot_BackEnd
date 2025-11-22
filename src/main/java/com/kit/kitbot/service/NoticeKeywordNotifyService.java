package com.kit.kitbot.service;

import com.kit.kitbot.document.NoticeKeyword;
import com.kit.kitbot.document.NoticeKeywordSubscription;
import com.kit.kitbot.document.Notification;
import com.kit.kitbot.repository.Notice.NoticeKeywordSubscriptionRepository;
import com.kit.kitbot.repository.Notice.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeKeywordNotifyService {

    private final NoticeKeywordSubscriptionRepository subRepo;
    private final NotificationRepository notificationRepo;
    private final PushSender pushSender;

    /**
     * "새 공지"가 크롤링되어 DB에 저장된 직후 호출되는 메서드
     * @param noticeId 크롤링 공지 ID
     * @param title 공지 제목
     * @param content 공지 본문(또는 요약)
     */
    public void notifySubscribers(String noticeId, String title, String content) {
        String text = normalize(title + " " + content);

        for (NoticeKeyword kw : NoticeKeyword.values()) {
            if (!matches(text, kw)) continue;

            // 해당 키워드 구독자(enabled=true) 조회
            List<NoticeKeywordSubscription> subs =
                    subRepo.findByKeywordAndEnabledTrue(kw);

            for (NoticeKeywordSubscription sub : subs) {
                String userId = sub.getUserId();

                // 중복 알림 방지
                boolean already =
                        notificationRepo.existsByUserIdAndNoticeIdAndKeyword(
                                userId, noticeId, kw.name()
                        );
                if (already) continue;

                // 알림 기록 생성
                Notification n = notificationRepo.save(Notification.builder()
                        .userId(userId)
                        .type("NOTICE_KEYWORD_MATCH")
                        .keyword(kw.name())
                        .noticeId(noticeId)
                        .title(title)
                        .pushed(false)
                        .read(false)
                        .build());

                // 푸시 전송
                boolean ok = pushSender.sendNoticeKeywordPush(userId, kw, noticeId, title);

                if (ok) {
                    n.markPushed();
                    notificationRepo.save(n);
                }
            }
        }
    }

    // ---------------- 내부 유틸 ----------------

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replaceAll("\\s+", ""); // 공백 제거
    }

    /** 고정 5개 키워드라 룰 기반 contains로 MVP */
    private boolean matches(String text, NoticeKeyword kw) {
        return switch (kw) {
            case SCHOLARSHIP -> text.contains("장학");
            case COURSE      -> text.contains("수강") || text.contains("학사") || text.contains("등록");
            case DORM        -> text.contains("생활관") || text.contains("기숙사");
            case EVENT       -> text.contains("행사") || text.contains("특강") || text.contains("세미나");
            case EMPLOYMENT  -> text.contains("취업") || text.contains("인턴") || text.contains("채용");
        };
    }
}
