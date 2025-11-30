package com.kit.kitbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LatestKeywordQuestionDTO {
    private String queryId;
    private String question;
    private String keyword;
    private LocalDateTime createdAt;
}
