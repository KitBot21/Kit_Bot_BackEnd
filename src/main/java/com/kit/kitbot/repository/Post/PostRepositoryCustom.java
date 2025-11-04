// src/main/java/com/kit/kitbot/repository/PostRepositoryCustom.java
package com.kit.kitbot.repository.Post;

import java.time.Instant;

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
}
