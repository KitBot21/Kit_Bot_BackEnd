package com.kit.kitbot.util;

import java.util.*;
import java.util.stream.Collectors;

public class KeywordNormalizer {

    // 키워드 하나 정규화
    public static Optional<String> normalizeOne(String raw) {
        if (raw == null) return Optional.empty();

        // 1) 앞뒤 공백 제거
        String s = raw.trim();

        // 2) 중복 공백 하나로
        s = s.replaceAll("\\s+", " ");

        // 3) 불필요 특수문자 제거 (원하면 규칙 추가 가능)
        s = s.replaceAll("[\"'#<>]", "");

        // 4) 영어는 소문자로 통일 (한글엔 영향 없음)
        s = s.toLowerCase(Locale.ROOT);

        // 5) 너무 짧은 키워드는 버림 (ex: "a", "?" 같은 노이즈)
        if (s.length() < 2) {
            return Optional.empty();
        }

        return Optional.of(s);
    }

    // 여러 개 한꺼번에 정규화
    public static List<String> normalizeAll(Collection<String> rawKeywords) {
        if (rawKeywords == null) return List.of();

        return rawKeywords.stream()
                .map(KeywordNormalizer::normalizeOne)
                .flatMap(Optional::stream)
                .distinct()  // 같은 키워드는 한 번만 카운트
                .collect(Collectors.toList());
    }
}
