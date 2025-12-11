package com.kit.kitbot.service;

import com.kit.kitbot.dto.PopularKeywordDto;
import com.kit.kitbot.util.KeywordNormalizer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class AnswerKeywordStatService {

    private static final String KEY_PREFIX = "popular:ans-keyword:";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public AnswerKeywordStatService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(LocalDate date) {
        return KEY_PREFIX + date.format(DATE_FORMAT);
    }

    public void increaseByRawKeywords(List<String> rawKeywords) {
        List<String> keywords = KeywordNormalizer.normalizeAll(rawKeywords);
        if (keywords.isEmpty()) {
            return;
        }

        String key = buildKey(LocalDate.now());
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();

        for (String kw : keywords) {
            zSet.incrementScore(key, kw, 1.0);
        }

        Long expire = redisTemplate.getExpire(key);
        if (expire == null || expire < 0) {
            redisTemplate.expire(key, TTL);
        }
    }


    public List<PopularKeywordDto> getTodayTop(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String key = buildKey(LocalDate.now());
        return getTopFromKey(key, limit);
    }

    private List<PopularKeywordDto> getTopFromKey(String key, int limit) {
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();

        Set<ZSetOperations.TypedTuple<String>> tuples =
                zSet.reverseRangeWithScores(key, 0, limit - 1);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        return tuples.stream()
                .map(t -> new PopularKeywordDto(
                        t.getValue(),
                        t.getScore() == null ? 0L : t.getScore().longValue()
                ))
                .toList();
    }
}