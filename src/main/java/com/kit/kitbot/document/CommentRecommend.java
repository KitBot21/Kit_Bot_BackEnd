// CommentRecommend.java
package com.kit.kitbot.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "comment_recommends")
public class CommentRecommend {

    @Id
    private String id;

    private String commentId;
    private String userId;
    private LocalDateTime createdAt = LocalDateTime.now();
}