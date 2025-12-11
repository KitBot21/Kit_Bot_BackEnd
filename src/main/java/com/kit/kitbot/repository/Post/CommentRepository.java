package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdAndStatus(String postId, String status);
    List<Comment> findByParentIdAndStatus(String parentId, String status);

    List<Comment> findByPostId(String postId);
}
