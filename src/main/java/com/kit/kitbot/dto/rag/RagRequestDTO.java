package com.kit.kitbot.dto.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagRequestDTO {
    private String query; // FastAPI가 받는 변수명 'query'
    @Builder.Default
    private int topk = 5; // 기본값 5
}