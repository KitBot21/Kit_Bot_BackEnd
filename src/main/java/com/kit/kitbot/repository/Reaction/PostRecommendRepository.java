package com.kit.kitbot.repository.Reaction;

import com.kit.kitbot.document.reaction.PostRecommend;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PostRecommendRepository extends MongoRepository<PostRecommend, String> {
    Optional<PostRecommend> findByPostIdAndUserId(String postId, String userId);
    boolean existsByPostIdAndUserId(String postId, String userId);
    long deleteByPostIdAndUserId(String postId, String userId);
}