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

    /* ===================== 유저용 ===================== */

    /* 게시글 무한 스크롤 조회 */
    @Operation(summary = "게시글 무한 스크롤 조회", description = """
            최신순(createdAt DESC)으로 게시글을 커서 기반으로 조회합니다.
            - 첫 호출은 after 없이 호출
            - 다음 호출은 응답의 nextCursor를 after 파라미터로 전달
            - limit 기본 10, 최대 50
            """)
    @GetMapping("/cursor")
    public ResponseEntity<CursorListResponseDTO<PostResponseDTO>> listCursor(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String after,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(postService.getPostsCursor(keyword, after, limit));
    }

    /** 게시글 작성 */
    @Operation(summary = "게시글 작성", description = "로그인된 사용자(role:kumoh)가 게시글을 작성합니다. (title, content 필수)")
    @PostMapping
    public ResponseEntity<PostResponseDTO> create(@RequestBody PostRequestDTO req) {
        // 보안 컨텍스트에서 현재 유저의 Mongo _id(String)를 가져와 authorId 주입
        String currentUserId = getCurrentUserId();
        req.setAuthorId(currentUserId);
        return ResponseEntity.ok(postService.createPost(req));
    }

    /** 단건 조회, 게시글 상세 조회 (일반 사용자: ACTIVE만) */
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 단건 조회합니다. ACTIVE 상태의 게시글만 조회됩니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getOne(@PathVariable String postId) {
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        Optional<PostResponseDTO> res = postService.getPost(postId, statuses);
        return res.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 전체 목록 조회 + 간단 키워드 검색 (일반 사용자: ACTIVE만) */
    @Operation(summary = "[Deprecated] 페이지네이션 목록 조회", description = "앱에서는 /api/posts/cursor (커서 기반) API 사용 권장")
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> list(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        Page<PostResponseDTO> page = (keyword == null || keyword.isBlank())
                ? postService.getPostList(statuses, pageable)
                : postService.searchPostsByTitle(keyword.trim(), statuses, pageable);
        return ResponseEntity.ok(page);
    }

    /** 내 글 목록 (마이페이지) */
    @Operation(summary = "내 글 목록 (마이페이지)", description = "자신이 작성한 게시글 조회")
    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDTO>> myPosts(Pageable pageable) {
        String userId = getCurrentUserId();
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        return ResponseEntity.ok(postService.getPostsByAuthor(userId, statuses, pageable));
    }

    /** 수정 (작성자만, 댓글 존재/블라인드/삭제 상태면 불가) */
    @Operation(summary = "게시글 수정", description = "작성자 본인만 수정할 수 있습니다. 블라인드/삭제된 게시글은 수정 불가. 댓글이 있는 게시물은 수정 불가.")
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> update(
            @PathVariable String postId,
            @RequestBody PostRequestDTO req
    ) {
        String editorId = getCurrentUserId();
        return ResponseEntity.ok(postService.updatePost(postId, editorId, req));
    }

    /** 소프트 삭제 (작성자만, 정책대로 댓글 있으면 불가) */
    @Operation(summary = "게시글 삭제", description = "작성자 본인만 소프트 삭제할 수 있습니다. 댓글이 있으면 삭제 불가.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> softDelete(@PathVariable String postId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.softDelete(postId, userId));
    }

    /** 추천 토글 (중복 방지) */
    @Operation(summary = "게시글 추천 토글", description = "추천 상태를 토글합니다. 이미 추천한 경우 취소됩니다.")
    @PostMapping("/{postId}/recommend/toggle")
    public ResponseEntity<PostResponseDTO> toggleRecommend(@PathVariable String postId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.toggleRecommend(postId, userId));
    }

    /** 신고 토글 (중복 방지; reason은 선택) */
    @Operation(summary = "게시글 신고 토글", description = "신고 상태를 토글합니다. 이유는 선택이며, 신고 누적 시 자동 블라인드 처리됩니다.")
    @PostMapping("/{postId}/report/toggle")
    public ResponseEntity<PostResponseDTO> toggleReport(
            @PathVariable String postId,
            @RequestParam(required = false) String reason
    ) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.toggleReport(postId, userId, reason));
    }

    /* ===================== 관리자용 ===================== */
    // 실제 접근 제어는 SecurityConfig에서 ROLE_ADMIN 등으로 제한해줘

    /** 관리자: 블라인드 처리 */
    @Operation(summary = "관리자: 게시글 블라인드 처리", description = "관리자가 게시글을 블라인드 처리합니다.")
    @PostMapping("/admin/{postId}/blind")
    public ResponseEntity<PostResponseDTO> adminBlind(
            @PathVariable String postId,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(postService.adminBlind(postId, reason));
    }

    /** 관리자: 언블라인드 처리 */
    @Operation(summary = "관리자: 게시글 언블라인드 처리", description = "블라인드된 게시글을 다시 활성화합니다.")
    @PostMapping("/admin/{postId}/unblind")
    public ResponseEntity<PostResponseDTO> adminUnblind(@PathVariable String postId) {
        return ResponseEntity.ok(postService.adminUnblind(postId));
    }

    /* ===================== Helper ===================== */

    /**
     * 현재 로그인한 사용자 Mongo _id를 문자열로 추출
     * 너희 보안 구현에 맞게 이 메서드만 한 줄로 교체하면 됨.
     */
    private String getCurrentUserId() {
        // 실제 로그인 구현 후 Principal에서 userId 추출하도록 교체 필요
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return auth.getName();
        }
        throw new IllegalStateException("로그인이 필요합니다. (보안 컨텍스트 비어있음)");
    }

}
