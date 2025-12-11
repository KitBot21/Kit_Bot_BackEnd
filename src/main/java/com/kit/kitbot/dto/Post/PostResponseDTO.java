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

    boolean isRecommended;
    boolean isReported;


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
                .isRecommended(isRecommended)
                .isReported(isReported)
                .build();
    }


    public static PostResponseDTO from(Post p, String authorNickname) {
        return PostResponseDTO.from(p, authorNickname, false, false);
    }


    public static PostResponseDTO from(Post p) {
        return PostResponseDTO.from(p, null, false, false);
    }
}