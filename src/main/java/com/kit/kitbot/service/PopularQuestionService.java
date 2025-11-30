package com.kit.kitbot.service;

import com.kit.kitbot.document.Query;
import com.kit.kitbot.dto.LatestKeywordQuestionDTO;
import com.kit.kitbot.repository.Query.QueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularQuestionService {

    private final QueryRepository queryRepository;

    public LatestKeywordQuestionDTO getLatestQuestionByKeyword(String keyword) {
        return queryRepository
                .findFirstByAnswerKeywordsContainingOrderByCreatedAtDesc(keyword)
                .map(q -> new LatestKeywordQuestionDTO(
                        q.getId(),
                        q.getQuestion(),
                        keyword,
                        q.getCreatedAt()
                ))
                .orElse(null);
    }
}
