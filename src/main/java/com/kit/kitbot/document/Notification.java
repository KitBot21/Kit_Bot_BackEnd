package com.kit.kitbot.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("notifications")
@CompoundIndex(
        name = "uk_user_notice_keyword",
        def = "{'userId': 1, 'noticeId': 1, 'keyword': 1}",
        unique = true
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    private String userId;

    private String type;        // "NOTICE_KEYWORD_MATCH"
    private String keyword;     // "SCHOLARSHIP" 같은 enum name 저장
    private String noticeId;    // 크롤링 공지 id
    private String title;       // 공지 제목

    @Builder.Default
    private boolean pushed = false; // 실제 푸시 성공 여부

    @Builder.Default
    private boolean read = false;   // (나중에 알림함용)

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void markPushed() {
        this.pushed = true;
    }

    public void markRead() {
        this.read = true;
    }
}