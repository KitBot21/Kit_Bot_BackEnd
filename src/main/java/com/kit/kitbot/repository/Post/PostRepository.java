package com.kit.kitbot.repository.Post;

import com.kit.kitbot.document.Post;
import com.kit.kitbot.document.Post.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom {

    Optional<Post> findByIdAndStatusIn(String id, Collection<Status> status);

    Page<Post> findByStatusIn(Collection<Status> statuses, Pageable pageable);

    Page<Post> findByAuthorIdAndStatusIn(String authorId, Collection<Status> statuses, Pageable pageable);

    Page<Post> findByTitleRegexAndStatusIn(String titleRegex, Collection<Status> statuses, Pageable pageable);
}
