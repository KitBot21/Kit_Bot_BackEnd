package com.kit.kitbot.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * 게시물 Document
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {

    /** 내부 식별자 PK */
    @Id
    private String id;

    /** 작성자 ID (users 컬렉션 FK) */
    @Indexed
    private Long authorId;

    /** 게시물 제목 */
    private String title;

    /** 본문 내용 */
    private String content;

    /** 추천(좋아요) 수 */
    @Builder.Default
    private int recommendCount = 0;

    /** 댓글 수 */
    @Builder.Default
    private int commentCount = 0;

    /** 신고 수 */
    @Builder.Default
    private int reportCount = 0;

    /** 작성 일시 */
    @CreatedDate
    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant createdAt;

    /** 수정 일시 */
    @LastModifiedDate
    private Instant updatedAt;

    /** 상태 (active, blinded, deleted) */
    @Builder.Default
    private Status status = Status.ACTIVE;

    /** 블라인드 일시 */
    private Instant blindedAt;

    /** 블라인드 사유 */
    private String blindedReason;

    public enum Status {
        ACTIVE,
        BLINDED,
        DELETED
    }
}
