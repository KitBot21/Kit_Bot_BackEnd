package com.kit.kitbot.util;

import java.util.*;
import java.util.stream.Collectors;

public class KeywordNormalizer {


    public static Optional<String> normalizeOne(String raw) {
        if (raw == null) return Optional.empty();


        String s = raw.trim();


        s = s.replaceAll("\\s+", " ");


        s = s.replaceAll("[\"'#<>]", "");


        s = s.toLowerCase(Locale.ROOT);


        if (s.length() < 2) {
            return Optional.empty();
        }

        return Optional.of(s);
    }


    public static List<String> normalizeAll(Collection<String> rawKeywords) {
        if (rawKeywords == null) return List.of();

        return rawKeywords.stream()
                .map(KeywordNormalizer::normalizeOne)
                .flatMap(Optional::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}
