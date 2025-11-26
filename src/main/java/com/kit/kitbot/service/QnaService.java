package com.kit.kitbot.service;

import com.kit.kitbot.document.Query;
import com.kit.kitbot.dto.QueryRequestDTO;
import com.kit.kitbot.dto.QueryResponseDTO;
import com.kit.kitbot.dto.SourceDTO;
import com.kit.kitbot.repository.Query.QueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 추가 권장

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final TranslationService translationService;
    private final QueryRepository queryRepository;
    // private final RAGServerClient ragServerClient;

    public QueryResponseDTO processQuestion(QueryRequestDTO requestDTO) {

        String originalQuestion = requestDTO.getQuestion();

        // 1. [언어 감지] 들어온 질문이 어느 나라 말인지 확인
        String detectedLang = translationService.detectLanguage(originalQuestion);
        System.out.println("감지된 언어: " + detectedLang);

        String finalQuestion = originalQuestion;

        // 2. [입력 번역] 한국어가 아니라면 -> 한글로 번역
        if (!"ko".equalsIgnoreCase(detectedLang)) {
            finalQuestion = translationService.translateText(originalQuestion, detectedLang, "ko");
            System.out.println("질문 번역(" + detectedLang + "->ko): " + finalQuestion);
        }

        // 3. [DB 저장] 한글로 변환된 질문 저장 (언어 정보는 감지된 걸로 저장)
        Query toSave = new Query(finalQuestion, detectedLang);
        queryRepository.save(toSave);

        // 4. [AI/Mock 처리] (한글 질문 -> 한글 답변)
        String mockAnswer = "이것은 '" + finalQuestion + "'에 대한 답변입니다.";
        List<SourceDTO> mockSources = List.of(
                new SourceDTO(1L, "공지사항", "http://kit.ac.kr")
        );

        // 5. [출력 번역] 사용자가 한국어 사용자가 아니었다면 -> 답변을 그 나라 말로 번역
        String finalAnswer = mockAnswer;

        if (!"ko".equalsIgnoreCase(detectedLang)) {
            finalAnswer = translationService.translateText(mockAnswer, "ko", detectedLang);
            System.out.println("답변 번역(ko->" + detectedLang + "): " + finalAnswer);
        }

        return new QueryResponseDTO(finalAnswer, mockSources);
    }
}