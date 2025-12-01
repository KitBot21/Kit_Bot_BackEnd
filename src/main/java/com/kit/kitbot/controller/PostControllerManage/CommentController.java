package com.kit.kitbot.controller.PostControllerManage;

import com.kit.kitbot.dto.CommentRequest;
import com.kit.kitbot.dto.CommentResponseDTO;
import com.kit.kitbot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성 - request body에서 authorId 받음
    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(@RequestBody CommentRequest request) {
        CommentResponseDTO response = commentService.createComment(request, request.getAuthorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글의 댓글 조회 (일반 댓글만) - 헤더에서 userId 받음
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable String postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPost(postId, userId);
        return ResponseEntity.ok(comments);
    }

    // 특정 댓글의 대댓글 조회 - 헤더에서 userId 받음
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponseDTO>> getReplies(
            @PathVariable String commentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        List<CommentResponseDTO> replies = commentService.getReplies(commentId, userId);
        return ResponseEntity.ok(replies);
    }

    // 댓글 추천 토글 - 헤더에서 userId 받음
    @PostMapping("/{commentId}/recommend/toggle")
    public ResponseEntity<Void> toggleRecommendComment(
            @PathVariable String commentId,
            @RequestHeader("X-User-Id") String userId) {
        commentService.toggleRecommendComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    // 댓글 신고 - 헤더에서 userId 받음
    @PostMapping("/{commentId}/report")
    public ResponseEntity<Void> reportComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") String userId) {
        String reason = request.get("reason");
        commentService.reportComment(commentId, userId, reason);
        return ResponseEntity.ok().build();
    }

    // 댓글 삭제 - 헤더에서 userId 받음
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @RequestHeader("X-User-Id") String userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
    }
}