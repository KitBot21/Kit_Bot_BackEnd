package com.kit.kitbot.dto.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;


@Value
@Builder
@AllArgsConstructor
public class CursorListResponseDTO<T> {

    List<T> items;

    String nextCursor;

    boolean hasNext;
}
