package com.kit.kitbot.repository.Notice;

import com.kit.kitbot.document.NoticeKeyword;
import com.kit.kitbot.document.NoticeKeywordSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeKeywordSubscriptionRepository
        extends MongoRepository<NoticeKeywordSubscription, String> {

    Optional<NoticeKeywordSubscription> findByUserIdAndKeyword(String userId, NoticeKeyword keyword);

    List<NoticeKeywordSubscription> findByKeywordAndEnabledTrue(NoticeKeyword keyword);

    List<NoticeKeywordSubscription> findByUserId(String userId);
}