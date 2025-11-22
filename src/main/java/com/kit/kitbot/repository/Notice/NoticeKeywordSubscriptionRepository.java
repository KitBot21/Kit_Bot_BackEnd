package com.kit.kitbot.repository.Notice;

import com.kit.kitbot.document.NoticeKeyword;
import com.kit.kitbot.document.NoticeKeywordSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeKeywordSubscriptionRepository
        extends MongoRepository<NoticeKeywordSubscription, String> {

    // 특정 사용자 + 특정 키워드
    Optional<NoticeKeywordSubscription> findByUserIdAndKeyword(String userId, NoticeKeyword keyword);

    // 특정 키워드를 enabled=true로 구독 중인 사용자 목록
    List<NoticeKeywordSubscription> findByKeywordAndEnabledTrue(NoticeKeyword keyword);

    // 한 사용자의 전체 구독 목록
    List<NoticeKeywordSubscription> findByUserId(String userId);
}