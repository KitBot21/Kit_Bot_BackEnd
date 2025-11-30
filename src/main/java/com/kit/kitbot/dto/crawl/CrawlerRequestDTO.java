package com.kit.kitbot.dto.crawl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerRequestDTO {
    private String url;     // 게시글 URL
    private String keyword; // 예: "SCHOLARSHIP", "DORM" (Enum 이름과 일치해야 함)
    private String title;   // 게시글 제목
}