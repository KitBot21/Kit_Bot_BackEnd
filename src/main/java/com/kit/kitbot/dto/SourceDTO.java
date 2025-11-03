package com.kit.kitbot.dto;

import lombok.Getter;
import lombok.AllArgsConstructor; // 모든 필드를 받는 생성자

@Getter
@AllArgsConstructor // Service에서 RAG 응답을 DTO로 만들 때 편리합니다.
public class SourceDTO {
    private Long docId;
    private String title;
    private String link;
}