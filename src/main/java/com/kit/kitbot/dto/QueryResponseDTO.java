package com.kit.kitbot.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
public class QueryResponseDTO {
    private String answer;
    private List<SourceDTO> sources;
}