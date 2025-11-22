package com.kit.kitbot.dto.Notice;

import lombok.*;

import java.util.List;

/**
 * PUT /api/notice-keywords/me
 * enabledKeywords에 들어온 키워드만 enabled=true
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveMyKeywordsRequestDto {
    private List<String> enabledKeywords; // ["SCHOLARSHIP", "EVENT"]
}
