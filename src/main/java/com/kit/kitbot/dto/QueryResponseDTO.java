package com.kit.kitbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
public class QueryResponseDTO {
    private String answer;
    private List<SourceDTO> sources;

    @JsonProperty("isDate")
    private boolean isDate;

    private String startDate;
    private String endDate;
    private String scheduleTitle;
}