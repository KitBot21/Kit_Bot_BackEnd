package com.kit.kitbot.controller.PostControllerManage;

import com.kit.kitbot.dto.CommentRequest;
import com.kit.kitbot.dto.CommentResponseDTO;
import com.kit.kitbot.security.CustomUserDetails;
import com.kit.kitbot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponseDTO response = commentService.createComment(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails != null ? userDetails.getUserId() : null;
        List<CommentResponseDTO> comments = commentService.getCommentsByPost(postId, userId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponseDTO>> getReplies(
            @PathVariable String commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails != null ? userDetails.getUserId() : null;
        List<CommentResponseDTO> replies = commentService.getReplies(commentId, userId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/{commentId}/recommend/toggle")
    public ResponseEntity<Void> toggleRecommendComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.toggleRecommendComment(commentId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/report")
    public ResponseEntity<Void> reportComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String reason = request.get("reason");
        commentService.reportComment(commentId, userDetails.getUserId(), reason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}