package com.kit.kitbot.dto.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class PostCursorRequestDTO {

    private String after;

    private Integer limit = 10;

    private String keyword;
}
