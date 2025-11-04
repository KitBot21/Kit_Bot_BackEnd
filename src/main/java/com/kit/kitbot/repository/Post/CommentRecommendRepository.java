// CommentRecommendRepository.java
package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.CommentRecommend;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRecommendRepository extends MongoRepository<CommentRecommend, String> {
    CommentRecommend findByCommentIdAndUserId(String commentId, String userId);
    void deleteByCommentIdAndUserId(String commentId, String userId);
}