package com.kit.kitbot.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeKeyword {
    SCHOLARSHIP("장학"),
    COURSE("학사/수강"),
    DORM("생활관"),
    EVENT("행사/특강"),
    EMPLOYMENT("취업/인턴");

    private final String label;
}
