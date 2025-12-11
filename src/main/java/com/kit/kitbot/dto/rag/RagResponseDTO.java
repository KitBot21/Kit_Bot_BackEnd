package com.kit.kitbot.dto.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class RagResponseDTO {
    private String keyword;

    private String message;

    private List<String> source;

    private List<String> link;

    @JsonProperty("isDate")
    private boolean isDate;

    private String startDate;

    private String endDate;

    private String scheduleTitle;
}