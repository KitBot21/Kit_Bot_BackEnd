package com.kit.kitbot.controller.PostControllerManage;

import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.dto.Post.PostRequestDTO;
import com.kit.kitbot.dto.Post.PostResponseDTO;
import com.kit.kitbot.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    /* ===================== 유저용 ===================== */

    /** 게시글 작성 */
    @PostMapping
    public ResponseEntity<PostResponseDTO> create(@RequestBody PostRequestDTO req) {
        // 보안 컨텍스트에서 현재 유저의 Mongo _id(String)를 가져와 authorId 주입
        String currentUserId = getCurrentUserId();
        req.setAuthorId(currentUserId);
        return ResponseEntity.ok(postService.createPost(req));
    }

    /** 단건 조회, 게시글 상세 조회 (일반 사용자: ACTIVE만) */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getOne(@PathVariable String postId) {
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        Optional<PostResponseDTO> res = postService.getPost(postId, statuses);
        return res.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 전체 목록 조회 + 간단 키워드 검색 (일반 사용자: ACTIVE만) */
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
    @GetMapping("/me")
    public ResponseEntity<Page<PostResponseDTO>> myPosts(Pageable pageable) {
        String userId = getCurrentUserId();
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        return ResponseEntity.ok(postService.getPostsByAuthor(userId, statuses, pageable));
    }

    /** 수정 (작성자만, 댓글 존재/블라인드/삭제 상태면 불가) */
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> update(
            @PathVariable String postId,
            @RequestBody PostRequestDTO req
    ) {
        String editorId = getCurrentUserId();
        return ResponseEntity.ok(postService.updatePost(postId, editorId, req));
    }

    /** 소프트 삭제 (작성자만, 정책대로 댓글 있으면 불가) */
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> softDelete(@PathVariable String postId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.softDelete(postId, userId));
    }

    /** 추천 토글 (중복 방지) */
    @PostMapping("/{postId}/recommend/toggle")
    public ResponseEntity<PostResponseDTO> toggleRecommend(@PathVariable String postId) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(postService.toggleRecommend(postId, userId));
    }

    /** 신고 토글 (중복 방지; reason은 선택) */
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
    @PostMapping("/admin/{postId}/blind")
    public ResponseEntity<PostResponseDTO> adminBlind(
            @PathVariable String postId,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(postService.adminBlind(postId, reason));
    }

    /** 관리자: 언블라인드 처리 */
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
        // 예시 1) 커스텀 Principal 사용 시
        // var auth = SecurityContextHolder.getContext().getAuthentication();
        // KitUserPrincipal p = (KitUserPrincipal) auth.getPrincipal();
        // return p.getUserId(); // String(ObjectId)

        // 예시 2) OAuth2 attributes에 userId가 있을 때
        // OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) auth;
        // return (String) oauth.getPrincipal().getAttributes().get("userId");

        throw new IllegalStateException("getCurrentUserId()를 보안 컨텍스트에 맞게 구현하세요.");
    }

}
