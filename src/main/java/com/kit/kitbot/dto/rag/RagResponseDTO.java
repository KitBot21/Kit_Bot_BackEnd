package com.kit.kitbot.dto.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class RagResponseDTO {
    // FastAPI: keyword (인기 키워드 집계용)
    private String keyword;

    // FastAPI: message (최종 답변) - 기존 answer에서 변경됨
    private String message;

    // FastAPI: source (문서 제목 리스트)
    private List<String> source;

    // FastAPI: link (원본 링크 리스트)
    private List<String> link;

    // FastAPI: isDate (캘린더 UI 활성화 여부)
    @JsonProperty("isDate") // JSON의 "isDate"를 확실하게 매핑
    private boolean isDate;

    private String startDate;

    // 👇 추가: 일정 종료일 (예: "2025-09-05")
    private String endDate;

    private String scheduleTitle;
}