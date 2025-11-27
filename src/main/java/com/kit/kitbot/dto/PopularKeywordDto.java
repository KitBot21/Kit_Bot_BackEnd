package com.kit.kitbot.dto;

public record PopularKeywordDto(
        String keyword, // "수강신청", "기숙사 환불" 같은 자유 키워드
        long count
) {}
