package com.kit.kitbot.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("notice_keyword_subscriptions")
@CompoundIndex(
        name = "uk_user_keyword",
        def = "{'userId': 1, 'keyword': 1}",
        unique = true
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NoticeKeywordSubscription {

    @Id
    private String id;

    private String userId;
    private NoticeKeyword keyword;
    private boolean enabled;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public void toggle() {
        this.enabled = !this.enabled;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }
}