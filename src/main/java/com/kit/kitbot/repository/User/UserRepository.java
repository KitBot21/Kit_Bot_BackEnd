package com.kit.kitbot.repository.User;

import com.kit.kitbot.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByGoogleEmail(String googleEmail);
}