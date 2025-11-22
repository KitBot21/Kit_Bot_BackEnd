package com.kit.kitbot.dto.Notice;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveMyKeywordsResponseDto {
    private List<MyKeywordSubscriptionDto> subscriptions;

    public static SaveMyKeywordsResponseDto of(List<MyKeywordSubscriptionDto> list) {
        return SaveMyKeywordsResponseDto.builder()
                .subscriptions(list)
                .build();
    }
}
