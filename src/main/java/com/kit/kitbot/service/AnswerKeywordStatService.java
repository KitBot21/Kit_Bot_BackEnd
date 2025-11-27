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

    private final StringRedisTemplate redisTemplate;
    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    public AnswerKeywordStatService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(LocalDate date) {
        return "popular:ans-keyword:" + date.format(DAY_FMT);
    }

    /**
     * 답변에서 온 "원본 키워드들"을 받아
     * 전처리 후 Redis Sorted Set에 카운트 올리는 메서드
     */
    public void increaseByRawKeywords(List<String> rawKeywords) {
        List<String> keywords = KeywordNormalizer.normalizeAll(rawKeywords);
        if (keywords.isEmpty()) return;

        String key = buildKey(LocalDate.now());

        for (String kw : keywords) {
            redisTemplate.opsForZSet()
                    .incrementScore(key, kw, 1.0);
        }

        // 선택: 2일 TTL (원하면 조정/삭제)
        redisTemplate.expire(key, Duration.ofDays(2));
    }

    /**
     * 오늘 기준 인기 키워드 Top N 조회
     */
    public List<PopularKeywordDto> getTodayTop(int limit) {
        String key = buildKey(LocalDate.now());

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(key, 0, limit - 1);

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
