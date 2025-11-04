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

    public static PostResponseDTO from(Post p) {
        return PostResponseDTO.builder()
                .id(p.getId())
                .authorId(p.getAuthorId())
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
                .build();
    }
}
