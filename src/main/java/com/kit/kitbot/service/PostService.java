package com.kit.kitbot.service;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.dto.Post.PostRequestDTO;
import com.kit.kitbot.dto.Post.PostResponseDTO;
import com.kit.kitbot.repository.Post.PostRepository;
import com.kit.kitbot.document.reaction.PostRecommend;
import com.kit.kitbot.document.reaction.PostReport;
import com.kit.kitbot.repository.Reaction.PostRecommendRepository;
import com.kit.kitbot.repository.Reaction.PostReportRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostRecommendRepository postRecommendRepository;
    private final PostReportRepository postReportRepository;
    private static final int REPORTS_TO_BLIND = 10; // 블라인드 처리 신고 개수 10

    /** 게시글 상세 조회 */
    public Optional<PostResponseDTO> getPost(String id, Collection<Status> statuses) {
        return postRepository.findByIdAndStatusIn(id, statuses).map(PostResponseDTO::from);
    }

    /** 게시글 목록 조회 */
    public Page<PostResponseDTO> getPostList(Collection<Status> statuses, Pageable pageable) {
        return postRepository.findByStatusIn(statuses, pageable).map(PostResponseDTO::from);
    }

    /** 작성자별 게시글 목록 조회 (마이페이지에서 사용) */
    public Page<PostResponseDTO> getPostsByAuthor(String authorId, Collection<Status> statuses, Pageable pageable) {
        return postRepository.findByAuthorIdAndStatusIn(authorId, statuses, pageable).map(PostResponseDTO::from);
    }

    /** 제목으로 게시글 검색 */
    public Page<PostResponseDTO> searchPostsByTitle(String keyword, Collection<Status> statuses, Pageable pageable) {
        String regex = ".*" + keyword + ".*";
        return postRepository.findByTitleRegexAndStatusIn(regex, statuses, pageable).map(PostResponseDTO::from);
    }

    /* 게시글 작성 -> ResponseDTO 반환 */
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO req) {
        if (req.getAuthorId() == null) throw new IllegalArgumentException("authorId는 필수입니다.");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new IllegalArgumentException("title은 필수입니다.");
        if (req.getContent() == null || req.getContent().isBlank()) throw new IllegalArgumentException("content는 필수입니다.");

        Instant now = Instant.now();
        Post post = new Post();
        post.setAuthorId(req.getAuthorId());
        post.setTitle(req.getTitle().trim());
        post.setContent(req.getContent());
        post.setStatus(Status.ACTIVE);
        post.setRecommendCount(0);
        post.setReportCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        post.setBlindedAt(null);
        post.setBlindedReason(null);

        return PostResponseDTO.from(postRepository.save(post));
    }

    /* 게시글 수정 -> 작성자만 수정 가능, 블라인드,삭제 상태시 수정 불가, 게시글내 댓글 존재 시 수정 불가 */
    @Transactional
    public PostResponseDTO updatePost(String postId, String editorUserId, PostRequestDTO req) {
        // 1) 대상 게시글 로드
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 없습니다: " + postId));

        // 2) 권한/상태 체크
        if (!post.getAuthorId().equals(editorUserId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("삭제된 게시물은 수정할 수 없습니다.");
        }
        if (post.getStatus() == Status.BLINDED) {
            throw new IllegalStateException("블라인드된 게시물은 수정할 수 없습니다.");
        }

        // 3) 댓글 존재 시 수정 불가
        int comments = post.getCommentCount();
        if (comments > 0) {
            throw new IllegalStateException("댓글이 있는 게시물은 수정할 수 없습니다.");
        }

        // 4) 부분 수정 (null/blank는 무시)
        boolean changed = false;
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            post.setTitle(req.getTitle().trim());
            changed = true;
        }
        if (req.getContent() != null && !req.getContent().isBlank()) {
            post.setContent(req.getContent());
            changed = true;
        }

        if (!changed) {
            return PostResponseDTO.from(post); // 변경사항 없으면 그대로 반환
        }

        post.setUpdatedAt(Instant.now());
        return PostResponseDTO.from(postRepository.save(post));
    }

    /* 게시글 소프트 삭제 -> 작성자만 가능, 이미 삭제면 불가, 댓글이 존재하면 삭제 불가 */
    @Transactional
    public PostResponseDTO softDelete(String postId, String requesterId) {
        // 1) 게시글 로드
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 없습니다: " + postId));

        // 2) 권한 체크 (작성자 본인만)
        if (!java.util.Objects.equals(post.getAuthorId(), requesterId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // 3) 상태 체크
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("이미 삭제된 게시물입니다.");
        }
        
        // 댓글이 있으면 삭제 불가
         if (post.getCommentCount() > 0) {
             throw new IllegalStateException("댓글이 있는 게시물은 삭제할 수 없습니다.");
         }

        // 4) 삭제 처리 (커스텀 리포지토리 사용)
        postRepository.softDelete(postId);

        // 5) 최신 상태 재조회 후 응답
        Post updated = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("삭제 처리 후 게시물 조회에 실패했습니다."));
        return PostResponseDTO.from(updated);
    }

    /* 공통: 업데이트 가능한 상태인지 확인 */
    private void ensureUpdatable(Post post) {
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("삭제된 게시물에는 작업할 수 없습니다.");
        }
        if (post.getStatus() == Status.BLINDED) {
            throw new IllegalStateException("블라인드된 게시물에는 작업할 수 없습니다.");
        }
    }

    /* 게시글 찾기(공통 메서드) */
    private Post mustFind(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 없습니다: " + postId));
    }

    /* 게시글 추천 토글방식(없으면 +1, 있으면 -1) */
    @Transactional
    public PostResponseDTO toggleRecommend(String postId, String userId) {
        Post post = mustFind(postId);
        ensureUpdatable(post);

        boolean exists = postRecommendRepository.existsByPostIdAndUserId(postId, userId);
        if (exists) {
            // 취소: 기록 삭제 & 카운터 -1
            postRecommendRepository.deleteByPostIdAndUserId(postId, userId);
            postRepository.incRecommendCount(postId, -1);
        } else {
            // 등록: 기록 생성 & 카운터 +1
            PostRecommend rec = new PostRecommend();
            rec.setPostId(postId);
            rec.setUserId(userId);
            rec.setCreatedAt(Instant.now());
            try {
                postRecommendRepository.save(rec);
                postRepository.incRecommendCount(postId, +1);
            } catch (DuplicateKeyException e) {
                // 동시성으로 인해 이미 존재하게 된 경우: 무시(멱등)
            }
        }
        return PostResponseDTO.from(mustFind(postId));
    }

    /* 게시글 신고 토글방식 (없으면 +1, 있으면 -1) */
    @Transactional
    public PostResponseDTO toggleReport(String postId, String userId, String reason) {
        Post post = mustFind(postId);
        ensureUpdatable(post);

        boolean exists = postReportRepository.existsByPostIdAndUserId(postId, userId);
        if (exists) {
            // 신고 취소 허용 정책: 취소하면 -1
            postReportRepository.deleteByPostIdAndUserId(postId, userId);
            postRepository.incReportCount(postId, -1);
        } else {
            // 신고 등록
            PostReport rep = new PostReport();
            rep.setPostId(postId);
            rep.setUserId(userId);
            rep.setReason(reason);
            rep.setCreatedAt(Instant.now());
            try {
                postReportRepository.save(rep);
                postRepository.incReportCount(postId, +1);

                // ★ 신고 수 증가 직후 자동 블라인드 체크
                autoBlindIfNeeded(postId);
            } catch (DuplicateKeyException e) {
                // 멱등 처리
            }
        }
        return PostResponseDTO.from(mustFind(postId));
    }

    /* 시스템 자동 블라인드 메서드 */
    private void autoBlindIfNeeded(String postId) {
        Post p = mustFind(postId);
        if (p.getStatus() == Status.ACTIVE && p.getReportCount() >= REPORTS_TO_BLIND) {
            postRepository.blind(postId, "자동 블라인드: 신고가 10회 이상 누적됨", Instant.now());
        }
    }

    /* 관리자: 블라인드 처리 */
    @Transactional
    public PostResponseDTO adminBlind(String postId, String reason) {
        Post post = mustFind(postId);
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("삭제된 게시물은 블라인드할 수 없습니다.");
        }
        if (post.getStatus() == Status.BLINDED) {
            return PostResponseDTO.from(post); // 이미 블라인드면 그대로
        }
        postRepository.blind(postId, reason != null ? reason : "관리자 블라인드", Instant.now());
        return PostResponseDTO.from(mustFind(postId));
    }

    /* 관리자: 언블라인드 처리(활성화 처리) */
    @Transactional
    public PostResponseDTO adminUnblind(String postId) {
        Post post = mustFind(postId);
        if (post.getStatus() != Status.BLINDED) {
            throw new IllegalStateException("블라인드된 게시물만 해제할 수 있습니다.");
        }

        postRepository.unblind(postId);
        return PostResponseDTO.from(mustFind(postId));
    }

}
