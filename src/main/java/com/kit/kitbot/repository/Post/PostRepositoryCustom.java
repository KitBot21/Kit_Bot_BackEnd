// src/main/java/com/kit/kitbot/repository/PostRepositoryCustom.java
package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface PostRepositoryCustom {

    /** 추천 수 증가/감소 (+1 / -1) */
    void incRecommendCount(String postId, int delta);

    /** 신고 수 증가/감소 */
    void incReportCount(String postId, int delta);

    /** 댓글 수 증가/감소 */
    void incCommentCount(String postId, int delta);

    /** 소프트 삭제 처리 (실제 삭제 대신 soft delete 사용 추후 복구, 감사 로그용) */
    void softDelete(String postId);

    /** 블라인드 처리 (관리자가 게시글을 블라인드 처리할 때 사용)*/
    void blind(String postId, String reason, Instant blindedAt);

    /** 언블라인드 처리 (관리자) */
    void unblind(String postId);

    /* ===== 커서 기반 조회(무한 스크롤) ===== */
    /**
     * 상태 집합(statuses) 필터 + (옵션) after createdAt 기준 + (옵션) 제목 keyword
     * 최신순(createdAt DESC, _id DESC)으로 limit개 가져온다.
     * hasNext 판정은 서비스에서 limit+1 규칙으로 처리 권장.
     */
    List<Post> findCursor(Set<Status> statuses, Instant after, int limit, String keyword);
}
