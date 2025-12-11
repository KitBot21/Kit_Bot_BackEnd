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



        boolean isRecommended = commentRecommendRepository
                .findByCommentIdAndUserId(saved.getId(), userId) != null;

        boolean isReported = commentReportRepository
                .existsByCommentIdAndUserId(saved.getId(), userId);

        return CommentResponseDTO.builder()
                .id(saved.getId())
                .postId(saved.getPostId())
                .authorId(saved.getAuthorId())
                .authorName(resolveAuthorName(saved.getAuthorId()))
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

    public List<CommentResponseDTO> getCommentsByPost(String postId, String currentUserId) {
        List<Comment> allComments = commentRepository.findByPostIdAndStatus(postId, "active");

        return allComments.stream()
                .filter(comment -> comment.getParentId() == null)
                .map(comment -> convertToResponse(comment, currentUserId))
                .toList();
    }

    public List<CommentResponseDTO> getReplies(String parentId, String currentUserId) {
        List<Comment> replies = commentRepository.findByParentIdAndStatus(parentId, "active");
        return replies.stream()
                .map(comment -> convertToResponse(comment, currentUserId))
                .toList();
    }


    private CommentResponseDTO convertToResponse(Comment comment, String currentUserId) {


        boolean isRecommended = false;
        boolean isReported = false;

        if (currentUserId != null && !currentUserId.isEmpty()) {
            isRecommended = commentRecommendRepository
                    .findByCommentIdAndUserId(comment.getId(), currentUserId) != null;
            isReported = commentReportRepository
                    .existsByCommentIdAndUserId(comment.getId(), currentUserId);
        }

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorName(resolveAuthorName(comment.getAuthorId()))
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .recommendCount(comment.getRecommendCount())
                .reportCount(comment.getReportCount())
                .createdAt(comment.getCreatedAt())
                .status(comment.getStatus())
                .isRecommended(isRecommended)
                .isReported(isReported)
                .build();
    }


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


    public void deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));


        if ("deleted".equals(comment.getStatus())) {
            throw new RuntimeException("이미 삭제된 댓글입니다");
        }


        comment.setStatus("deleted");
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);


        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }


    public void softDeleteByAdmin(String commentId, String adminId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));


        if ("deleted".equalsIgnoreCase(comment.getStatus())) {
            return;
        }


        comment.setStatus("deleted");
        comment.setUpdatedAt(LocalDateTime.now());

        comment.setBlindedReason(reason);

        commentRepository.save(comment);


        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
    }

    private String resolveAuthorName(String authorId) {
        if (authorId == null || authorId.isBlank()) return "알 수 없는 사용자";

        return userRepository.findById(authorId)
                .map(user -> {
                    if (user.getStatus() == User.Status.deleted) {
                        return "탈퇴한 사용자";
                    }
                    return user.getUsername() != null ? user.getUsername() : "알 수 없는 사용자";
                })
                .orElse("탈퇴한 사용자");
    }

}
