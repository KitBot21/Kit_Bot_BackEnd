// src/main/java/com/kit/kitbot/repository/PostRepositoryImpl.java
package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    /** 관리자: 언블라인드 처리 */
    @Override
    public void unblind(String postId) {
        Query q = byId(postId);
        Update u = new Update()
                .set("status", Status.ACTIVE)
                .set("blindedAt", null)
                .set("blindedReason", null)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Post.class);
    }

    /* ===== 커서 기반 조회 구현 (무한 스크롤용) ===== */
    @Override
    public List<Post> findCursor(Set<Status> statuses, Instant after, int limit, String keyword) {
        List<Criteria> ands = new ArrayList<>();
        ands.add(Criteria.where("status").in(statuses));

        if (after != null) {
            // after 보다 "더 과거" 데이터(스크롤 다운) = createdAt < after
            ands.add(Criteria.where("createdAt").lt(after));
        }

        if (keyword != null && !(keyword = keyword.trim()).isBlank()) {
            // 제목에 keyword 포함 (대소문자 무시)
            ands.add(Criteria.where("title").regex(keyword, "i"));
        }

        Query q = new Query(new Criteria().andOperator(ands.toArray(new Criteria[0])))
                // 안정 정렬: createdAt DESC, _id DESC
                .with(Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "_id")))
                .limit(limit);

        return mongoTemplate.find(q, Post.class);
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
