package com.kit.kitbot.dto.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString
public class PostRequestDTO {
    private String authorId;
    private String title;
    private String content;
}
