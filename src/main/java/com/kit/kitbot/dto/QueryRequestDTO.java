package com.kit.kitbot.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QueryRequestDTO {
    private String question;
    private String appLanguage;
}