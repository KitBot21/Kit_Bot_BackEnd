package com.kit.kitbot.repository.User;

import com.kit.kitbot.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByGoogleEmail(String googleEmail);
    boolean existsByUsername(String username);
    List<User> findByStatusAndDeletedAtBefore(User.Status status, Instant date);
    boolean existsBySchoolEmail(String schoolEmail);
    List<User> findAllByPushToken(String pushToken);

}