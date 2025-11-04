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
    private static final String TEMP_USER_ID = "6908b0ea11c4a31b7f814a5a";

    // 댓글 작성
    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(@RequestBody CommentRequest request) {
        CommentResponseDTO response = commentService.createComment(request, TEMP_USER_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글의 댓글 조회 (일반 댓글만)
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable String postId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    // 특정 댓글의 대댓글 조회
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponseDTO>> getReplies(@PathVariable String commentId) {
        List<CommentResponseDTO> replies = commentService.getReplies(commentId);
        return ResponseEntity.ok(replies);
    }

    // CommentController.java

    @PostMapping("/{commentId}/recommend/toggle")
    public ResponseEntity<Void> toggleRecommendComment(@PathVariable String commentId) {
        commentService.toggleRecommendComment(commentId, TEMP_USER_ID);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/report")
    public ResponseEntity<Void> reportComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        commentService.reportComment(commentId, TEMP_USER_ID, reason);
        return ResponseEntity.ok().build();
    }


}
