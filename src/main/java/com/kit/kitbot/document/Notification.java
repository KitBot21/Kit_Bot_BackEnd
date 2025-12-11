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

    private String type;
    private String keyword;
    private String noticeId;
    private String title;

    @Builder.Default
    private boolean pushed = false;

    @Builder.Default
    private boolean read = false;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void markPushed() {
        this.pushed = true;
    }

    public void markRead() {
        this.read = true;
    }
}