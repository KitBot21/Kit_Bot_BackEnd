package com.kit.kitbot.document.reaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@Document(collection = "post_reports")
@CompoundIndex(name = "uk_post_user", def = "{'postId': 1, 'userId': 1}", unique = true)
public class PostReport {
    @Id
    private String id;
    private String postId;
    private String userId;
    private String reason;
    private Instant createdAt;
}
