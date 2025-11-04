package com.kit.kitbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// CommentResponseDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {
    private String id;
    private String postId;
    private String authorId;
    private String authorName;
    private String content;
    private String parentId;
    private Integer recommendCount;
    private Integer reportCount;
    private LocalDateTime createdAt;
    private String status;
    private Boolean isRecommended;  // 추가
    private Boolean isReported;     // 추가
}