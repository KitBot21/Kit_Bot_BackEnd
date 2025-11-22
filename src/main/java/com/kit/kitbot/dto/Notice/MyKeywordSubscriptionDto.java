package com.kit.kitbot.dto.Notice;

import com.kit.kitbot.document.NoticeKeywordSubscription;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyKeywordSubscriptionDto {
    private String keyword;   // "SCHOLARSHIP"
    private boolean enabled;

    public static MyKeywordSubscriptionDto from(NoticeKeywordSubscription sub) {
        return MyKeywordSubscriptionDto.builder()
                .keyword(sub.getKeyword().name())
                .enabled(sub.isEnabled())
                .build();
    }
}
