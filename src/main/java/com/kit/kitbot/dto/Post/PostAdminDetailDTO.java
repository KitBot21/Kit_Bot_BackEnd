package com.kit.kitbot.dto.Post;

import com.kit.kitbot.document.Comment;
import com.kit.kitbot.document.Post;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
@Builder
public class PostAdminDetailDTO {

    String id;
    String title;
    String content;
    String authorId;
    String authorNickname;
    int recommendCount;
    int reportCount;
    int commentCount;
    Instant createdAt;
    Post.Status status;
    Instant blindedAt;
    String blindedReason;

    List<CommentNode> comments;

    @Value
    @Builder
    public static class CommentNode {
        String id;
        String postId;
        String authorId;
        String authorNickname;
        String parentId;
        String content;
        Integer recommendCount;
        Integer reportCount;
        LocalDateTime createdAt;
        String status;

        List<CommentNode> children;
    }

    public static PostAdminDetailDTO of(
            Post post,
            List<Comment> comments,
            Map<String, String> userIdToNickname
    ) {

        Map<String, CommentNode> nodeMap = new LinkedHashMap<>();
        for (Comment c : comments) {
            CommentNode node = CommentNode.builder()
                    .id(c.getId())
                    .postId(c.getPostId())
                    .authorId(c.getAuthorId())
                    .authorNickname(userIdToNickname.getOrDefault(
                            c.getAuthorId(),
                            c.getAuthorId()
                    ))
                    .parentId(c.getParentId())
                    .content(c.getContent())
                    .recommendCount(c.getRecommendCount())
                    .reportCount(c.getReportCount())
                    .createdAt(c.getCreatedAt())
                    .status(c.getStatus())
                    .children(new ArrayList<>())
                    .build();
            nodeMap.put(node.getId(), node);
        }

        List<CommentNode> roots = new ArrayList<>();
        for (CommentNode node : nodeMap.values()) {
            String parentId = node.getParentId();
            if (parentId != null && nodeMap.containsKey(parentId)) {
                nodeMap.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        return PostAdminDetailDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthorId())
                .authorNickname(userIdToNickname.getOrDefault(
                        post.getAuthorId(),
                        post.getAuthorId()
                ))
                .recommendCount(post.getRecommendCount())
                .reportCount(post.getReportCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .status(post.getStatus())
                .blindedAt(post.getBlindedAt())
                .blindedReason(post.getBlindedReason())
                .comments(roots)
                .build();
    }
}

