package com.kit.kitbot.dto.Notice;

import com.kit.kitbot.document.NoticeKeyword;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeKeywordItemDto {
    private String key;
    private String label;

    public static NoticeKeywordItemDto from(NoticeKeyword kw) {
        return NoticeKeywordItemDto.builder()
                .key(kw.name())
                .label(kw.getLabel())
                .build();
    }
}
