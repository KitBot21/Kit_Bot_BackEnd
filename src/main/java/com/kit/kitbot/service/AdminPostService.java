package com.kit.kitbot.service;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.repository.Post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final PostRepository postRepository;

    /**
     * 관리자용 게시글 목록 조회
     *
     * @param status  상태 필터 (ACTIVE, BLINDED, DELETED, ALL/null → 전체)
     * @param keyword 제목 키워드 검색 (null/빈문자열이면 검색 X)
     */
    public Page<Post> searchPosts(String status, String keyword, Pageable pageable) {

        // 1) 상태 리스트 구성
        List<Status> statuses;
        if (status == null || status.isBlank()
                || status.equalsIgnoreCase("ALL")) {
            statuses = List.copyOf(EnumSet.allOf(Status.class)); // ACTIVE, BLINDED, DELETED 전체
        } else {
            // 대소문자 구분 없이 enum 매핑
            Status s = Status.valueOf(status.toUpperCase(Locale.ROOT));
            statuses = List.of(s);
        }

        // 2) 키워드 유무에 따라 분기
        if (keyword != null && !keyword.isBlank()) {
            // MongoDB regex: .*keyword.*
            String escaped = Pattern.quote(keyword);
            String regex = ".*" + escaped + ".*";
            return postRepository.findByTitleRegexAndStatusIn(regex, statuses, pageable);
        }

        // 3) 키워드 없으면 상태만 필터
        return postRepository.findByStatusIn(statuses, pageable);
    }

    /** 게시글 소프트 삭제 (status = DELETED) */
    public void softDelete(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.softDelete();
        postRepository.save(post);
    }

    /** 게시글 언블라인드 해제 (BLINDED → ACTIVE) */
    public void unblind(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.unblind();
        postRepository.save(post);
    }
}
