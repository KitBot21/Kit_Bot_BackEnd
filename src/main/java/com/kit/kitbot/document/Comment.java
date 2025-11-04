package com.kit.kitbot.document;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    private String postId;
    private String authorId;
    private String parentId;

    private String content;
    private Integer recommendCount = 0;
    private Integer reportCount = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String status = "active";  // active, blinded, deleted
    private LocalDateTime blindedAt;
    private String blindedReason;
}
