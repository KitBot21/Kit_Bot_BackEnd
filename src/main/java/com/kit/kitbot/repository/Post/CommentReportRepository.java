package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.CommentReport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentReportRepository extends MongoRepository<CommentReport, String> {
    boolean existsByCommentIdAndUserId(String commentId, String userId);
}