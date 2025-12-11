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

    @Override
    public void incRecommendCount(String postId, int delta) {
        updateCounter(postId, "recommendCount", delta);
    }


    @Override
    public void incReportCount(String postId, int delta) {
        updateCounter(postId, "reportCount", delta);
    }

    @Override
    public void incCommentCount(String postId, int delta) {
        updateCounter(postId, "commentCount", delta);
    }

    @Override
    public void softDelete(String postId) {
        Query q = byId(postId);
        Update u = new Update()
                .set("status", Status.DELETED)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Post.class);
    }

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

    @Override
    public List<Post> findCursor(Set<Status> statuses, Instant after, int limit, String keyword) {
        List<Criteria> ands = new ArrayList<>();
        ands.add(Criteria.where("status").in(statuses));

        if (after != null) {
            ands.add(Criteria.where("createdAt").lt(after));
        }

        if (keyword != null && !(keyword = keyword.trim()).isBlank()) {
            ands.add(Criteria.where("title").regex(keyword, "i"));
        }

        Query q = new Query(new Criteria().andOperator(ands.toArray(new Criteria[0])))
                .with(Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "_id")))
                .limit(limit);

        return mongoTemplate.find(q, Post.class);
    }


    private void updateCounter(String postId, String field, int delta) {
        Query q = byId(postId);
        Update u = new Update()
                .inc(field, delta)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Post.class);
    }

    private Query byId(String postId) {
        return new Query(Criteria.where("_id").is(postId));
    }
}
