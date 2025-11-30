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

    // Redis 키 prefix: popular:ans-keyword:YYYYMMDD
    private static final String KEY_PREFIX = "popular:ans-keyword:";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    // 하루 단위 통계지만, 여유 있게 7일 TTL
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public AnswerKeywordStatService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 날짜별 Redis 키 생성
    private String buildKey(LocalDate date) {
        return KEY_PREFIX + date.format(DATE_FORMAT);
    }

    /**
     * 🔹 RAG에서 온 "원본 키워드들"을 정규화해서,
     *    오늘 날짜 기준 ZSET에 카운트(+1) 증가
     */
    public void increaseByRawKeywords(List<String> rawKeywords) {
        // 1) 정규화 (null, 너무 짧은 문자열 등 필터링)
        List<String> keywords = KeywordNormalizer.normalizeAll(rawKeywords);
        if (keywords.isEmpty()) {
            return;
        }

        // 2) 오늘 날짜 기준 키
        String key = buildKey(LocalDate.now());
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();

        // 3) 각 키워드 score +1
        for (String kw : keywords) {
            zSet.incrementScore(key, kw, 1.0);
        }

        // 4) TTL 설정 (이미 TTL 있으면 그대로 두기)
        Long expire = redisTemplate.getExpire(key);
        if (expire == null || expire < 0) {
            redisTemplate.expire(key, TTL);
        }
    }

    /**
     * 🔹 오늘 기준 상위 N개 키워드 조회
     */
    public List<PopularKeywordDto> getTodayTop(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        String key = buildKey(LocalDate.now());
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
