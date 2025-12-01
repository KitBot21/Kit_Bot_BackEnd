package com.kit.kitbot.dto.Post;

import com.kit.kitbot.document.Post;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PostResponseDTO {
    String id;
    String authorId;
    String authorNickname;
    String title;
    String content;
    String status;
    Integer recommendCount;
    Integer reportCount;
    Integer commentCount;
    Instant createdAt;
    Instant updatedAt;
    Instant blindedAt;
    String blindedReason;

    // 👇 [추가] 상세 조회 시 사용될 필드
    boolean isRecommended;
    boolean isReported;

    /**
     * [상세용]
     * Service가 모든 비즈니스 로직(isRecommended 등)을 계산한 후 호출하는 메서드
     */
    public static PostResponseDTO from(Post p, String authorNickname, boolean isRecommended, boolean isReported) {
        return PostResponseDTO.builder()
                .id(p.getId())
                .authorId(p.getAuthorId())
                .authorNickname(authorNickname)
                .title(p.getTitle())
                .content(p.getContent())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .recommendCount(p.getRecommendCount())
                .reportCount(p.getReportCount())
                .commentCount(p.getCommentCount())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .blindedAt(p.getBlindedAt())
                .blindedReason(p.getBlindedReason())
                .isRecommended(isRecommended) // 👇 값 할당
                .isReported(isReported)       // 👇 값 할당
                .build();
    }

    /**
     * [목록용] 닉네임 포함 / 개인화 false
     */
    public static PostResponseDTO from(Post p, String authorNickname) {
        return PostResponseDTO.from(p, authorNickname, false, false);
    }

    /**
     * [목록용] (오버로딩)
     * Service가 목록 조회 시 호출하는 단순 변환 메서드
     * (개인화 정보는 기본값 false로 고정)
     */
    public static PostResponseDTO from(Post p) {
        // 닉네임 정보 없이, isRecommended / isReported = false 기본값
        return PostResponseDTO.from(p, null, false, false);
    }
}