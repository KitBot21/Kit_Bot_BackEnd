package com.kit.kitbot.repository.Reaction;

import com.kit.kitbot.document.reaction.PostReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List; // 👈 [추가]
import java.util.Optional;

public interface PostReportRepository extends MongoRepository<PostReport, String> {
    // 👇 [수정] Long -> String
    Optional<PostReport> findByPostIdAndUserId(String postId, String userId);
    boolean existsByPostIdAndUserId(String postId, String userId);
    long deleteByPostIdAndUserId(String postId, String userId);


}