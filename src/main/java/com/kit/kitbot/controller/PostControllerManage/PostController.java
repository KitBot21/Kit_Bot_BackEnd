package com.kit.kitbot.controller.PostControllerManage;

import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.dto.Post.CursorListResponseDTO;
import com.kit.kitbot.dto.Post.PostRequestDTO;
import com.kit.kitbot.dto.Post.PostResponseDTO;
import com.kit.kitbot.security.CustomUserDetails;  // 👈 추가
import com.kit.kitbot.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 👈 추가
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Tag(name = "게시글", description = "게시글 작성, 조회, 수정, 삭제 및 관리 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 무한 스크롤 조회")
    @GetMapping("/cursor")
    public ResponseEntity<CursorListResponseDTO<PostResponseDTO>> listCursor(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String after,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(postService.getPostsCursor(keyword, after, limit));
    }

    @Operation(summary = "게시글 작성")
    @PostMapping
    public ResponseEntity<PostResponseDTO> create(
            @RequestBody PostRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        req.setAuthorId(userDetails.getUserId());
        return ResponseEntity.ok(postService.createPost(req));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getOne(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        String currentUserId = userDetails != null ? userDetails.getUserId() : null;

        Optional<PostResponseDTO> res = postService.getPost(postId, statuses, currentUserId);
        return res.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "내 글 목록 (마이페이지)")
    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDTO>> myPosts(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        return ResponseEntity.ok(postService.getPostsByAuthor(userDetails.getUserId(), statuses, pageable));
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> update(
            @PathVariable String postId,
            @RequestBody PostRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.updatePost(postId, userDetails.getUserId(), req));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> softDelete(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.softDelete(postId, userDetails.getUserId()));
    }

    @Operation(summary = "게시글 추천 토글")
    @PostMapping("/{postId}/recommend/toggle")
    public ResponseEntity<PostResponseDTO> toggleRecommend(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.toggleRecommend(postId, userDetails.getUserId()));
    }

    @Operation(summary = "게시글 신고 토글")
    @PostMapping("/{postId}/report/toggle")
    public ResponseEntity<PostResponseDTO> toggleReport(
            @PathVariable String postId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.toggleReport(postId, userDetails.getUserId(), reason));
    }

    @Operation(summary = "관리자: 게시글 블라인드 처리")
    @PostMapping("/admin/{postId}/blind")
    public ResponseEntity<PostResponseDTO> adminBlind(
            @PathVariable String postId,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(postService.adminBlind(postId, reason));
    }

    @Operation(summary = "관리자: 게시글 언블라인드 처리")
    @PostMapping("/admin/{postId}/unblind")
    public ResponseEntity<PostResponseDTO> adminUnblind(@PathVariable String postId) {
        return ResponseEntity.ok(postService.adminUnblind(postId));
    }
}