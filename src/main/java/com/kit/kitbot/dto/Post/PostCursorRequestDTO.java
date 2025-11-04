package com.kit.kitbot.dto.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 무한 스크롤용 게시글 목록 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PostCursorRequestDTO {

    /** 커서 기준 시각 (createdAt) */
    private String after; // ISO 8601 문자열 ("2025-11-04T03:20:00Z") 형식

    /** 가져올 개수 (limit) */
    private Integer limit = 10; // 기본값

    /** 제목 검색 키워드 (선택) */
    private String keyword;
}
