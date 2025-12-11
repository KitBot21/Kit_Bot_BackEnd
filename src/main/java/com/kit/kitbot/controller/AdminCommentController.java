package com.kit.kitbot.controller;

import com.kit.kitbot.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @PatchMapping("/{commentId}/soft-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "관리자 댓글 soft delete",
            description = "관리자가 댓글/대댓글을 소프트 삭제합니다. (작성자와 무관)"
    )
    public void softDeleteCommentByAdmin(
            @PathVariable String commentId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(name = "X-Admin-Id", required = false) String adminId
    ) {
        String reason = body != null ? body.getOrDefault("reason", null) : null;
        commentService.softDeleteByAdmin(commentId, adminId, reason);
    }
}
