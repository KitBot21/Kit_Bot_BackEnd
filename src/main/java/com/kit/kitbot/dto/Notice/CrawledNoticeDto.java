package com.kit.kitbot.dto.Notice;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawledNoticeDto {
    private String noticeId;
    private String title;
    private String content;
}
