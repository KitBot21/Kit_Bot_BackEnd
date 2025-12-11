package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface PostRepositoryCustom {

    void incRecommendCount(String postId, int delta);

    void incReportCount(String postId, int delta);

    void incCommentCount(String postId, int delta);

    void softDelete(String postId);

    void blind(String postId, String reason, Instant blindedAt);

    void unblind(String postId);


    List<Post> findCursor(Set<Status> statuses, Instant after, int limit, String keyword);
}
