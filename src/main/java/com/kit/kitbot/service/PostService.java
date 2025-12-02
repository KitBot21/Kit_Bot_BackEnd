package com.kit.kitbot.service;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.document.User;
import com.kit.kitbot.dto.Post.PostRequestDTO;
import com.kit.kitbot.dto.Post.PostResponseDTO;
import com.kit.kitbot.dto.Post.CursorListResponseDTO;
import com.kit.kitbot.document.reaction.PostRecommend;
import com.kit.kitbot.document.reaction.PostReport;
import com.kit.kitbot.repository.Post.PostRepository;
import com.kit.kitbot.repository.Reaction.PostRecommendRepository;
import com.kit.kitbot.repository.Reaction.PostReportRepository;
import com.kit.kitbot.repository.User.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostRecommendRepository postRecommendRepository;
    private final PostReportRepository postReportRepository;
    private final UserRepository userRepository;

    private static final int REPORTS_TO_BLIND = 10;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    // ---------------- 공통 유틸 ----------------

    private int normalizeLimit(Integer limit) {
        if (limit == null) return DEFAULT_LIMIT;
        if (limit < 1) return 1;
        return Math.min(limit, MAX_LIMIT);
    }

    private Instant parseAfter(String afterIso8601) {
        if (afterIso8601 == null || afterIso8601.isBlank()) return null;
        try {
            return Instant.parse(afterIso8601.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("after 커서 형식이 올바르지 않습니다(ISO-8601 필요): " + afterIso8601);
        }
    }

    private String safeNextCursor(List<Post> rows, boolean hasNext) {
        if (!hasNext || rows.isEmpty()) return null;
        Instant last = rows.get(rows.size() - 1).getCreatedAt();
        return last != null ? last.toString() : null;
    }

//    private String resolveAuthorNickname(String authorId) {
//        if (authorId == null || authorId.isBlank()) return null;
//
//        return userRepository.findById(authorId)
//                .map(user -> {
//                    // 닉네임으로 쓸 값 결정 로직
//                    if (user.hasUsername()) {
//                        return user.getUsername();          // ⭐ 사용자가 설정한 닉네임
//                    }
//                    // username 미설정이면 googleEmail 앞부분 같은 걸로 대체하거나,
//                    // 그냥 고정 문구를 써도 됨
//                    String email = user.getGoogleEmail();
//                    if (email != null && !email.isBlank()) {
//                        int at = email.indexOf('@');
//                        return at > 0 ? email.substring(0, at) : email;
//                    }
//                    return "알 수 없는 사용자";
//                })
//                .orElse("탈퇴한 사용자");
//    }
    private String resolveAuthorNickname(String authorId) {
        if (authorId == null || authorId.isBlank()) return null;

        return userRepository.findById(authorId)
            .map(user -> {
                // ⭐ 탈퇴한 사용자 체크 추가!
                if (user.getStatus() == User.Status.deleted) {
                    return "탈퇴한 사용자";
                }

                if (user.hasUsername()) {
                    return user.getUsername();
                }
                String email = user.getGoogleEmail();
                if (email != null && !email.isBlank()) {
                    int at = email.indexOf('@');
                    return at > 0 ? email.substring(0, at) : email;
                }
                return "알 수 없는 사용자";
            })
            .orElse("탈퇴한 사용자");
    }

    // ---------------- 조회 (커서) ----------------

    public CursorListResponseDTO<PostResponseDTO> getPostsCursor(String keyword, String after, Integer limit) {
        final int take = normalizeLimit(limit);
        final Instant afterTs = parseAfter(after);
        final Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        final int querySize = take + 1;
        final String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        List<Post> rows = postRepository.findCursor(statuses, afterTs, querySize, kw);

        boolean hasNext = rows.size() > take;
        if (hasNext) {
            rows = rows.subList(0, take);
        }

        String nextCursor = safeNextCursor(rows, hasNext);

        List<PostResponseDTO> items = rows.stream()
                .map(p -> {
                    String nickname = resolveAuthorNickname(p.getAuthorId());
                    return PostResponseDTO.from(p, nickname);
                })
                .collect(Collectors.toList());

        return new CursorListResponseDTO<>(items, nextCursor, hasNext);
    }

    // ---------------- 단건 조회 ----------------

    @Transactional(readOnly = true)
    public Optional<PostResponseDTO> getPost(String id, Collection<Status> statuses, String currentUserId) {
        return postRepository.findByIdAndStatusIn(id, statuses)
                .map(post -> buildDtoWithUserData(post, currentUserId));
    }

    // ---------------- 페이지네이션 조회 ----------------

    public Page<PostResponseDTO> getPostList(Collection<Status> statuses, Pageable pageable) {
        return postRepository.findByStatusIn(statuses, pageable)
                .map(post -> PostResponseDTO.from(post, resolveAuthorNickname(post.getAuthorId())));
    }

    public Page<PostResponseDTO> getPostsByAuthor(String authorId, Collection<Status> statuses, Pageable pageable) {
        return postRepository.findByAuthorIdAndStatusIn(authorId, statuses, pageable)
                .map(post -> PostResponseDTO.from(post, resolveAuthorNickname(post.getAuthorId())));
    }

    public Page<PostResponseDTO> searchPostsByTitle(String keyword, Collection<Status> statuses, Pageable pageable) {
        String regex = ".*" + keyword + ".*";
        return postRepository.findByTitleRegexAndStatusIn(regex, statuses, pageable)
                .map(post -> PostResponseDTO.from(post, resolveAuthorNickname(post.getAuthorId())));
    }

    // ---------------- 생성/수정/삭제 ----------------

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO req) {
        if (req.getAuthorId() == null) throw new IllegalArgumentException("authorId는 필수입니다.");
        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new IllegalArgumentException("title은 필수입니다.");
        if (req.getContent() == null || req.getContent().isBlank())
            throw new IllegalArgumentException("content는 필수입니다.");

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

        Post savedPost = postRepository.save(post);
        return buildDtoWithUserData(savedPost, savedPost.getAuthorId());
    }

    @Transactional
    public PostResponseDTO updatePost(String postId, String editorUserId, PostRequestDTO req) {
        Post post = mustFind(postId);

        if (!post.getAuthorId().equals(editorUserId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        ensureUpdatable(post);

        int comments = post.getCommentCount();
        if (comments > 0) {
            throw new IllegalStateException("댓글이 있는 게시물은 수정할 수 없습니다.");
        }

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
            return buildDtoWithUserData(post, editorUserId);
        }

        post.setUpdatedAt(Instant.now());
        return buildDtoWithUserData(postRepository.save(post), editorUserId);
    }

    @Transactional
    public PostResponseDTO softDelete(String postId, String requesterId) {
        Post post = mustFind(postId);

        if (!Objects.equals(post.getAuthorId(), requesterId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("이미 삭제된 게시물입니다.");
        }
        if (post.getCommentCount() > 0) {
            throw new IllegalStateException("댓글이 있는 게시물은 삭제할 수 없습니다.");
        }

        postRepository.softDelete(postId);
        Post updated = mustFind(postId);
        return buildDtoWithUserData(updated, requesterId);
    }

    // ---------------- 추천/신고 ----------------

    private void ensureUpdatable(Post post) {
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("삭제된 게시물에는 작업할 수 없습니다.");
        }
        if (post.getStatus() == Status.BLINDED) {
            throw new IllegalStateException("블라인드된 게시물에는 작업할 수 없습니다.");
        }
    }

    private Post mustFind(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 없습니다: " + postId));
    }

    @Transactional
    public PostResponseDTO toggleRecommend(String postId, String userId) {
        Post post = mustFind(postId);
        ensureUpdatable(post);

        boolean exists = postRecommendRepository.existsByPostIdAndUserId(postId, userId);
        if (exists) {
            postRecommendRepository.deleteByPostIdAndUserId(postId, userId);
            postRepository.incRecommendCount(postId, -1);
        } else {
            PostRecommend rec = new PostRecommend();
            rec.setPostId(postId);
            rec.setUserId(userId);
            rec.setCreatedAt(Instant.now());
            try {
                postRecommendRepository.save(rec);
                postRepository.incRecommendCount(postId, +1);
            } catch (DuplicateKeyException e) {
                // 중복 추천은 무시
            }
        }
        return buildDtoWithUserData(mustFind(postId), userId);
    }

    @Transactional
    public PostResponseDTO toggleReport(String postId, String userId, String reason) {
        Post post = mustFind(postId);
        ensureUpdatable(post);

        boolean exists = postReportRepository.existsByPostIdAndUserId(postId, userId);
        if (exists) {
            postReportRepository.deleteByPostIdAndUserId(postId, userId);
            postRepository.incReportCount(postId, -1);
        } else {
            PostReport rep = new PostReport();
            rep.setPostId(postId);
            rep.setUserId(userId);
            rep.setReason(reason);
            rep.setCreatedAt(Instant.now());
            try {
                postReportRepository.save(rep);
                postRepository.incReportCount(postId, +1);
                autoBlindIfNeeded(postId);
            } catch (DuplicateKeyException e) {
                // 중복 신고는 무시
            }
        }
        return buildDtoWithUserData(mustFind(postId), userId);
    }

    private void autoBlindIfNeeded(String postId) {
        Post p = mustFind(postId);
        if (p.getStatus() == Status.ACTIVE && p.getReportCount() >= REPORTS_TO_BLIND) {
            postRepository.blind(postId, "자동 블라인드: 신고가 10회 이상 누적됨", Instant.now());
        }
    }

    // ---------------- 관리자 블라인드 ----------------

    @Transactional
    public PostResponseDTO adminBlind(String postId, String reason) {
        Post post = mustFind(postId);
        if (post.getStatus() == Status.DELETED) {
            throw new IllegalStateException("삭제된 게시물은 블라인드할 수 없습니다.");
        }
        if (post.getStatus() == Status.BLINDED) {
            return buildDtoWithUserData(post, null);
        }
        postRepository.blind(postId, reason != null ? reason : "관리자 블라인드", Instant.now());
        return buildDtoWithUserData(mustFind(postId), null);
    }

    @Transactional
    public PostResponseDTO adminUnblind(String postId) {
        Post post = mustFind(postId);
        if (post.getStatus() != Status.BLINDED) {
            throw new IllegalStateException("블라인드된 게시물만 해제할 수 있습니다.");
        }
        postRepository.unblind(postId);
        return buildDtoWithUserData(mustFind(postId), null);
    }

    // ---------------- DTO 빌드 ----------------

    private PostResponseDTO buildDtoWithUserData(Post post, String currentUserId) {
        if (post == null) return null;

        String authorNickname = resolveAuthorNickname(post.getAuthorId());

        // 로그인 정보가 없으면 추천/신고 여부 계산 안 함
        if (currentUserId == null || currentUserId.isBlank()) {
            return PostResponseDTO.from(post, authorNickname);
        }

        boolean isRec = postRecommendRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        boolean isRep = postReportRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        return PostResponseDTO.from(post, authorNickname, isRec, isRep);
    }
}
