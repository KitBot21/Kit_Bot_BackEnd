package com.kit.kitbot.service;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Comment;
import com.kit.kitbot.document.Post.Status;
import com.kit.kitbot.document.User;
import com.kit.kitbot.dto.Post.PostAdminDetailDTO;
import com.kit.kitbot.repository.Post.PostRepository;
import com.kit.kitbot.repository.Post.CommentRepository;
import com.kit.kitbot.repository.User.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public Page<Post> searchPosts(String status, String keyword, Pageable pageable) {

        List<Status> statuses;
        if (status == null || status.isBlank()
                || status.equalsIgnoreCase("ALL")) {
            statuses = List.copyOf(EnumSet.allOf(Status.class));
        } else {
            Status s = Status.valueOf(status.toUpperCase(Locale.ROOT));
            statuses = List.of(s);
        }

        if (keyword != null && !keyword.isBlank()) {
            // MongoDB regex: .*keyword.*
            String escaped = Pattern.quote(keyword);
            String regex = ".*" + escaped + ".*";
            return postRepository.findByTitleRegexAndStatusIn(regex, statuses, pageable);
        }


        return postRepository.findByStatusIn(statuses, pageable);
    }


    public void softDelete(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.softDelete();
        postRepository.save(post);
    }


    public void unblind(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.unblind();
        postRepository.save(post);
    }


    public PostAdminDetailDTO getPostDetail(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<Comment> comments = commentRepository.findByPostId(postId);


        Set<String> authorIds = new HashSet<>();
        authorIds.add(post.getAuthorId());
        comments.forEach(c -> authorIds.add(c.getAuthorId()));


        List<User> users = userRepository.findAllById(authorIds);


        Map<String, String> userIdToNickname = users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> {
                            String username = u.getUsername();
                            return (username != null && !username.isBlank())
                                    ? username
                                    : u.getGoogleEmail();
                        }
                ));

        return PostAdminDetailDTO.of(post, comments, userIdToNickname);
    }
}
