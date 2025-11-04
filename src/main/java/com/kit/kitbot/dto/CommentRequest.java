package com.kit.kitbot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class CommentRequest {
    private String postId;
    private String content;
    private String parentId;

}
