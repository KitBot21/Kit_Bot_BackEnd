package com.kit.kitbot.dto.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 무한 스크롤용 커서 기반 응답 DTO
 */
@Value
@Builder
@AllArgsConstructor
public class CursorListResponseDTO<T> {

    /** 실제 데이터 목록 */
    List<T> items;

    /** 다음 커서(createdAt or ObjectId) */
    String nextCursor;

    /** 다음 페이지가 존재하는지 여부 */
    boolean hasNext;
}
