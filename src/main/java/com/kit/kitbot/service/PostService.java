package com.kit.kitbot.service;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.repository.Post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    /** 게시글 상세 조회 */
    public Optional<Post> getPost(String id, Collection<Status> statuses) {
        return postRepository.findByIdAndStatusIn(id, statuses);
    }

    /** 게시글 목록 조회 */
    public Page<Post> getPostList(Collection<Status> statuses, Pageable pageable) {
        return postRepository.findByStatusIn(statuses, pageable);
    }

    /** 작성자별 게시글 목록 조회 (마이페이지에서 사용) */
    public Page<Post> getPostsByAuthor(Long authorId, Collection<Status> statuses, Pageable pageable) {
        return postRepository.findByAuthorIdAndStatusIn(authorId, statuses, pageable);
    }

    /** 제목으로 게시글 검색 */
    public Page<Post> searchPostsByTitle(String keyword, Collection<Status> statuses, Pageable pageable) {
        // 정규식 검색 (MongoDB는 대소문자 무시 regex 가능)
        String regex = ".*" + keyword + ".*";
        return postRepository.findByTitleRegexAndStatusIn(regex, statuses, pageable);
    }

    // 이후에 작성할 내용 ↓
    // - 게시글 생성
    // - 게시글 수정
    // - 추천수/댓글수/신고수 업데이트
    // - 소프트 삭제
    // - 블라인드 처리
}
