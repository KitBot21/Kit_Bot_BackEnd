package com.kit.kitbot.dto.Post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString
public class PostRequestDTO {
    private String authorId;   // 보안상 서버에서 주입 권장. 임시로 허용해도 됨.
    private String title;
    private String content;
}
