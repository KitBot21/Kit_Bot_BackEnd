package com.kit.kitbot.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class SourceDTO {
    private Long docId;
    private String title;
    private String link;
}