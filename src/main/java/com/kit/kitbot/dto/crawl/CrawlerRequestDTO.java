package com.kit.kitbot.dto.crawl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerRequestDTO {
    private String url;
    private String keyword;
    private String title;
}