package com.kit.kitbot.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.*;

import java.time.Instant;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_status_createdAt_id",
                def = "{'status': 1, 'createdAt': -1, '_id': -1}"
        )
})
public class Post {

    @Id
    private String id;

    @Indexed
    private String authorId;

    private String title;

    private String content;

    @Builder.Default
    private int recommendCount = 0;

    @Builder.Default
    private int commentCount = 0;

    @Builder.Default
    private int reportCount = 0;

    @CreatedDate
    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder.Default
    private Status status = Status.ACTIVE;

    private Instant blindedAt;

    private String blindedReason;

    public enum Status {
        ACTIVE,
        BLINDED,
        DELETED
    }


    public void softDelete() {
        this.status = Status.DELETED;
    }

    public void blind(String reason) {
        this.status = Status.BLINDED;
        this.blindedAt = Instant.now();
        this.blindedReason = reason;
    }

    public void unblind() {
        if (this.status == Status.BLINDED) {
            this.status = Status.ACTIVE;
            this.blindedAt = null;
            this.blindedReason = null;
        }
    }
}
