package com.kit.kitbot.util;

import java.util.*;
import java.util.stream.Collectors;

public class KeywordNormalizer {

    // 키워드 하나 정규화
    public static Optional<String> normalizeOne(String raw) {
        if (raw == null) return Optional.empty();

        String s = raw.trim();               // 1) 앞뒤 공백 제거
        s = s.replaceAll("\\s+", " ");       // 2) 중복 공백 하나로
        s = s.replaceAll("[\"'#<>]", "");    // 3) 불필요 특수문자 제거 (원하면 수정)

        if (s.length() < 2) {                // 4) 너무 짧으면 버림
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
