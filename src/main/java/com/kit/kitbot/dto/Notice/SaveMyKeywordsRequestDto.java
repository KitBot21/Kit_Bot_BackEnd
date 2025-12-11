package com.kit.kitbot.dto.Notice;

import lombok.*;

import java.util.List;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveMyKeywordsRequestDto {
    private List<String> enabledKeywords;
}
