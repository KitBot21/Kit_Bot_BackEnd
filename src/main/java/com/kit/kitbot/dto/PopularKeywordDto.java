package com.kit.kitbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실시간 인기 답변 키워드 정보 DTO")
public class PopularKeywordDto {

    @Schema(description = "답변에서 추출된 키워드 문구", example = "수강신청 일정")
    private String keyword;

    @Schema(description = "해당 키워드가 집계된 횟수 (오늘 기준)", example = "12")
    private long count;
}
