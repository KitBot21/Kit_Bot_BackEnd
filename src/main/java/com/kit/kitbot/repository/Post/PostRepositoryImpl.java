// src/main/java/com/kit/kitbot/repository/PostRepositoryImpl.java
package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    /* 추천 수 증가/감소 */
    @Override
    public void incRecommendCount(String postId, int delta) {
        updateCounter(postId, "recommendCount", delta);
    }

    /* 신고 수 증가/감소 */
    @Override
    public void incReportCount(String postId, int delta) {
        updateCounter(postId, "reportCount", delta);
    }

    /* 댓글 수 증가/감소 */
    @Override
    public void incCommentCount(String postId, int delta) {
        updateCounter(postId, "commentCount", delta);
    }

    /* 소프트 삭제 */
    @Override
    public void softDelete(String postId) {
        Query q = byId(postId);
        Update u = new Update()
                .set("status", Status.DELETED)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Post.class);
    }

    /* 게시글 블라인드 처리 */
    @Override
    public void blind(String postId, String reason, Instant blindedAt) {
        Query q = byId(postId);
        Update u = new Update()
                .set("status", Status.BLINDED)
                .set("blindedAt", blindedAt != null ? blindedAt : Instant.now())
                .set("blindedReason", reason)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Post.class);
    }

    /* ===== private helpers ===== */
    /* 공통 로직: 추천, 신고, 댓글 수 업데이트에 사용 */
    private void updateCounter(String postId, String field, int delta) {
        Query q = byId(postId);
        Update u = new Update()
                .inc(field, delta)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Post.class);
    }

    /* MongoDB에서 _id 필드로 해당 문서를 찾기위한 공통 쿼리 */
    private Query byId(String postId) {
        return new Query(Criteria.where("_id").is(postId));
    }
}
