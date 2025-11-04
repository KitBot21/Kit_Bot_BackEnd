package com.kit.kitbot.controller.PostControllerManage;

import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.dto.Post.CursorListResponseDTO;
import com.kit.kitbot.dto.Post.PostRequestDTO;
import com.kit.kitbot.dto.Post.PostResponseDTO;
import com.kit.kitbot.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<PostResponseDTO> create(@RequestBody PostRequestDTO req) {
        String currentUserId = getCurrentUserId();
        req.setAuthorId(currentUserId);
        return ResponseEntity.ok(postService.createPost(req));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getOne(@PathVariable String postId) {
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        String currentUserId = getCurrentUserId();

        System.out.println("=== GET ONE POST ===");
        System.out.println("postId: " + postId);
        System.out.println("currentUserId: " + currentUserId);

        Optional<PostResponseDTO> res = postService.getPost(postId, statuses, currentUserId);

        res.ifPresent(dto -> {
            System.out.println("Response isRecommended: " + dto.isRecommended());
            System.out.println("Response isReported: " + dto.isReported());
        });

        return res.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "내 글 목록 (마이페이지)")
    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDTO>> myPosts(Pageable pageable) {
        String userId = getCurrentUserId();
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        return ResponseEntity.ok(postService.getPostsByAuthor(userId, statuses, pageable));
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> update(
            @PathVariable String postId,
            @RequestBody PostRequestDTO req
    ) {
        String editorId = getCurrentUserId();
        return ResponseEntity.ok(postService.updatePost(postId, editorId, req));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> softDelete(@PathVariable String postId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.softDelete(postId, userId));
    }

    @Operation(summary = "게시글 추천 토글")
    @PostMapping("/{postId}/recommend/toggle")
    public ResponseEntity<PostResponseDTO> toggleRecommend(@PathVariable String postId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.toggleRecommend(postId, userId));
    }

    @Operation(summary = "게시글 신고 토글")
    @PostMapping("/{postId}/report/toggle")
    public ResponseEntity<PostResponseDTO> toggleReport(
            @PathVariable String postId,
            @RequestParam(required = false) String reason
    ) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.toggleReport(postId, userId, reason));
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

    private String getCurrentUserId() {
        final String TEMP_TEST_USER_ID = "6908b0ea11c4a31b7f814a5a";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                return TEMP_TEST_USER_ID;
            }
            return auth.getName();
        }
        return TEMP_TEST_USER_ID;
    }
}

//    private String getCurrentUserId() {
//        // 실제 로그인 구현 후 Principal에서 userId 추출하도록 교체 필요
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
//            return auth.getName();
//        }
//        throw new IllegalStateException("로그인이 필요합니다. (보안 컨텍스트 비어있음)");
//    }

/** 전체 목록 조회 + 간단 키워드 검색 (일반 사용자: ACTIVE만) */
//    @Operation(summary = "[Deprecated] 페이지네이션 목록 조회", description = "앱에서는 /api/posts/cursor (커서 기반) API 사용 권장")
//    @GetMapping
//    public ResponseEntity<Page<PostResponseDTO>> list(
//            @RequestParam(required = false) String keyword,
//            Pageable pageable
//    ) {
//        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
//        Page<PostResponseDTO> page = (keyword == null || keyword.isBlank())
//                ? postService.getPostList(statuses, pageable)
//                : postService.searchPostsByTitle(keyword.trim(), statuses, pageable);
//        return ResponseEntity.ok(page);
//    }
