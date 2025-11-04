package com.kit.kitbot.service;

import com.kit.kitbot.document.*;
import com.kit.kitbot.dto.CommentRequest;
import com.kit.kitbot.dto.CommentResponseDTO;
import com.kit.kitbot.repository.Post.CommentRecommendRepository;
import com.kit.kitbot.repository.Post.CommentReportRepository;
import com.kit.kitbot.repository.Post.CommentRepository;
import com.kit.kitbot.repository.Post.PostRepository;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentReportRepository commentReportRepository;
    private final CommentRecommendRepository commentRecommendRepository;
    private final PostRepository postRepository;

    public CommentResponseDTO createComment(CommentRequest request, String userId) {
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다"));

            if ("deleted".equals(parentComment.getStatus())) {
                throw new RuntimeException("삭제된 댓글에는 답글을 달 수 없습니다");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setAuthorId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        boolean isRecommended = commentRecommendRepository
                .findByCommentIdAndUserId(saved.getId(), userId) != null;

        boolean isReported = commentReportRepository
                .existsByCommentIdAndUserId(saved.getId(), userId);

        return CommentResponseDTO.builder()
                .id(saved.getId())
                .postId(saved.getPostId())
                .authorId(saved.getAuthorId())
                .authorName(user.getUsername())
                .content(saved.getContent())
                .parentId(saved.getParentId())
                .recommendCount(saved.getRecommendCount())
                .reportCount(saved.getReportCount())
                .createdAt(saved.getCreatedAt())
                .status(saved.getStatus())
                .isRecommended(isRecommended)
                .isReported(isReported)
                .build();
    }

    public List<CommentResponseDTO> getCommentsByPost(String postId) {
        List<Comment> allComments = commentRepository.findByPostIdAndStatus(postId, "active");

        return allComments.stream()
                .filter(comment -> comment.getParentId() == null)
                .map(this::convertToResponse)  // ← 이 메서드 필요!
                .toList();
    }

    public List<CommentResponseDTO> getReplies(String parentId) {
        List<Comment> replies = commentRepository.findByParentIdAndStatus(parentId, "active");
        return replies.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ← 이 메서드 추가!
    // CommentService.java
    private CommentResponseDTO convertToResponse(Comment comment) {
        User user = userRepository.findById(comment.getAuthorId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 현재 사용자의 추천/신고 여부 확인
        String currentUserId = "6908b0ea11c4a31b7f814a5a";  // 임시 사용자 ID

        boolean isRecommended = commentRecommendRepository
                .findByCommentIdAndUserId(comment.getId(), currentUserId) != null;

        boolean isReported = commentReportRepository
                .existsByCommentIdAndUserId(comment.getId(), currentUserId);

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorName(user.getUsername())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .recommendCount(comment.getRecommendCount())
                .reportCount(comment.getReportCount())
                .createdAt(comment.getCreatedAt())
                .status(comment.getStatus())
                .isRecommended(isRecommended)      // 추가
                .isReported(isReported)            // 추가
                .build();
    }

    // CommentService.java

    public void toggleRecommendComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));
        CommentRecommend recommend = commentRecommendRepository
                .findByCommentIdAndUserId(commentId, userId);

        if (recommend != null) {
            commentRecommendRepository.delete(recommend);
            comment.setRecommendCount(Math.max(0, comment.getRecommendCount() - 1));
        } else {
            CommentRecommend newRecommend = new CommentRecommend();
            newRecommend.setCommentId(commentId);
            newRecommend.setUserId(userId);
            commentRecommendRepository.save(newRecommend);
            comment.setRecommendCount(comment.getRecommendCount() + 1);
        }

        commentRepository.save(comment);
    }

    public void reportComment(String commentId, String userId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));

        CommentReport report = new CommentReport();
        report.setCommentId(commentId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setCreatedAt(LocalDateTime.now());

        commentReportRepository.save(report);
        comment.setReportCount(comment.getReportCount() + 1);
        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));

        // TODO: 로그인 구현 후 권한 확인 활성화
        // 권한 확인 (본인의 댓글만 삭제 가능)
        // if (!comment.getAuthorId().equals(userId)) {
        //     throw new RuntimeException("댓글 삭제 권한이 없습니다");
        // }

        // 이미 삭제된 댓글인지 확인
        if ("deleted".equals(comment.getStatus())) {
            throw new RuntimeException("이미 삭제된 댓글입니다");
        }

        // 소프트 삭제 (status를 deleted로 변경)
        comment.setStatus("deleted");
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // 게시글의 댓글 수 감소
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }

}
