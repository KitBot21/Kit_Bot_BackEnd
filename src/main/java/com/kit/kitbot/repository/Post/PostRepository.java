package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom {

    /** 상세 조회(일반 사용자 노출용): 삭제 글 제외 */
    Optional<Post> findByIdAndStatusIn(String id, Collection<Status> status);

    /** 전체 목록(일반 사용자 노출용): 삭제 글 제외, 페이징 */
    Page<Post> findByStatusIn(Collection<Status> statuses, Pageable pageable);

    /** 작성자별 목록 (마이페이지에서 사용) */
    Page<Post> findByAuthorIdAndStatusIn(String authorId, Collection<Status> statuses, Pageable pageable);

    /** 제목 키워드 검색(간단 버전) */
    Page<Post> findByTitleRegexAndStatusIn(String titleRegex, Collection<Status> statuses, Pageable pageable);
}
