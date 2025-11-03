package com.kit.kitbot.service;

import com.kit.kitbot.dto.QueryRequestDTO;
import com.kit.kitbot.dto.QueryResponseDTO;
import com.kit.kitbot.dto.SourceDTO;
//import com.kit.kitbot.client.RAGServerClient; // (가상) FastAPI 통신
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List; // List 임포트

@Service
@RequiredArgsConstructor
public class QnaService {

//    private final RAGServerClient ragServerClient;
    // ... (RedisClient, ChatQuestionRepository 등)

    public QueryResponseDTO processQuestion(QueryRequestDTO requestDTO) {

        // RAGResponseDTO ragResponse = ragServerClient.processQuestion(requestDTO.getQuestion());

        String mockAnswer = "'" + requestDTO.getQuestion() + "'에 대한 답변입니다.";
        List<SourceDTO> mockSources = List.of(
                new SourceDTO(1L, "KIT 공지사항", "http://www.kumoh.ac.kr"),
                new SourceDTO(2L, "KIT 학사일정", "http://www.kumoh.ac.kr/calendar")
        );

        // QueryResponseDTO response = new QueryResponseDTO(ragResponse.getAnswer(), ragResponse.getSources());


        QueryResponseDTO response = new QueryResponseDTO(mockAnswer, mockSources);

        // logKeywords(ragResponse.getKeywords());
        return response;
    }
}