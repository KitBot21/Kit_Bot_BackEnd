package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdAndStatus(String postId, String status);
    List<Comment> findByParentIdAndStatus(String parentId, String status);

    // 👇 관리자용: 상태 상관없이 해당 게시글의 모든 댓글
    List<Comment> findByPostId(String postId);
}
