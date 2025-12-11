package com.kit.kitbot.repository.Query;

import com.kit.kitbot.document.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface QueryRepository extends MongoRepository<Query, String> {

    Optional<Query> findFirstByAnswerKeywordsContainingOrderByCreatedAtDesc(String keyword);
}
