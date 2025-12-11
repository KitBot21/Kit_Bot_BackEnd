package com.kit.kitbot.service;

import com.kit.kitbot.document.NoticeKeyword;
import com.kit.kitbot.document.NoticeKeywordSubscription;
import com.kit.kitbot.repository.Notice.NoticeKeywordSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeKeywordSubscriptionService {

    private final NoticeKeywordSubscriptionRepository subRepo;


    public List<NoticeKeywordSubscription> getMySubscriptions(String userId) {
        return subRepo.findByUserId(userId);
    }


    public NoticeKeywordSubscription toggle(String userId, NoticeKeyword keyword) {
        Optional<NoticeKeywordSubscription> opt =
                subRepo.findByUserIdAndKeyword(userId, keyword);

        NoticeKeywordSubscription sub;
        if (opt.isPresent()) {
            sub = opt.get();
            sub.toggle();
        } else {
            sub = NoticeKeywordSubscription.builder()
                    .userId(userId)
                    .keyword(keyword)
                    .enabled(true)
                    .build();
        }
        return subRepo.save(sub);
    }


    public List<NoticeKeywordSubscription> saveAll(String userId, Set<NoticeKeyword> enabledKeywords) {

        List<NoticeKeywordSubscription> existing = subRepo.findByUserId(userId);
        Map<NoticeKeyword, NoticeKeywordSubscription> map =
                existing.stream().collect(Collectors.toMap(NoticeKeywordSubscription::getKeyword, x -> x));

        List<NoticeKeywordSubscription> result = new ArrayList<>();

        for (NoticeKeyword kw : NoticeKeyword.values()) {
            NoticeKeywordSubscription sub = map.get(kw);

            boolean shouldEnable = enabledKeywords.contains(kw);

            if (sub == null) {
                sub = NoticeKeywordSubscription.builder()
                        .userId(userId)
                        .keyword(kw)
                        .enabled(shouldEnable)
                        .build();
            } else {
                if (shouldEnable) sub.enable();
                else sub.disable();
            }
            result.add(subRepo.save(sub));
        }

        return result;
    }
}
