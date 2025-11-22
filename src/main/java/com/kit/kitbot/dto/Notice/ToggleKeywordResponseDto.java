package com.kit.kitbot.dto.Notice;

import com.kit.kitbot.document.NoticeKeywordSubscription;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToggleKeywordResponseDto {
    private String keyword;
    private boolean enabled;

    public static ToggleKeywordResponseDto from(NoticeKeywordSubscription sub) {
        return ToggleKeywordResponseDto.builder()
                .keyword(sub.getKeyword().name())
                .enabled(sub.isEnabled())
                .build();
    }
}
