package com.kit.kitbot.repository.Query;

import com.kit.kitbot.document.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface QueryRepository extends MongoRepository<Query, String> {

    // answerKeywords 배열 안에 keyword 포함된 것 중 가장 최근 1개
    Optional<Query> findFirstByAnswerKeywordsContainingOrderByCreatedAtDesc(String keyword);
}
