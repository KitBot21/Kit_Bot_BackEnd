package com.kit.kitbot.controller;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.service.AdminPostService;
import com.kit.kitbot.dto.Post.PostAdminDetailDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 게시글 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@Tag(
        name = "관리자 게시글 관리 API",
        description = "관리자가 게시글 목록을 조회하고, 게시글을 소프트 삭제 / 언블라인드 해제하는 API"
)
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    @Operation(
            summary = "게시글 목록 조회",
            description = """
                    관리자용 게시글 전체 목록 조회 API입니다.
                    - status (ACTIVE, BLINDED, DELETED, ALL)
                    - keyword (제목 키워드 검색)
                    페이징(Pageable)을 지원합니다.
                    """
    )
    public Page<Post> getPosts(
            @Parameter(description = "게시글 상태 필터 (ACTIVE, BLINDED, DELETED, ALL 가능). 기본값 = ALL")
            @RequestParam(required = false) String status,

            @Parameter(description = "제목 키워드 검색. 공백/NULL이면 검색 없이 전체 조회")
            @RequestParam(required = false) String keyword,

            @ParameterObject Pageable pageable
    ) {
        return adminPostService.searchPosts(status, keyword, pageable);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "게시글 소프트 삭제",
            description = """
                    지정한 게시글을 소프트 삭제합니다.
                    실제 DB에서 삭제되지는 않고 상태(Status)가 DELETED로 변경됩니다.
                    """
    )
    public void softDeletePost(
            @Parameter(description = "삭제할 게시글 ID", required = true)
            @PathVariable String postId
    ) {
        adminPostService.softDelete(postId);
    }

    @PatchMapping("/{postId}/unblind")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "게시글 언블라인드 해제",
            description = """
                    BLINDED 상태인 게시글을 ACTIVE 상태로 되돌립니다.
                    블라인드 사유와 블라인드 일시는 초기화됩니다.
                    """
    )
    public void unblindPost(
            @Parameter(description = "언블라인드 해제할 게시글 ID", required = true)
            @PathVariable String postId
    ) {
        adminPostService.unblind(postId);
    }

    @GetMapping("/{postId}/detail")
    @Operation(
            summary = "관리자용 게시글 상세 조회",
            description = "게시글 정보와 댓글/대댓글 전체를 포함하여 조회합니다. (삭제/블라인드 포함)"
    )
    public PostAdminDetailDTO getPostDetail(
            @Parameter(description = "조회할 게시글 ID", required = true)
            @PathVariable String postId
    ) {
        return adminPostService.getPostDetail(postId);
    }
}
