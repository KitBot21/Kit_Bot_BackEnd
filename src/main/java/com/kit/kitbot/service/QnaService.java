package com.kit.kitbot.service;

import com.kit.kitbot.client.RagWebClient;
import com.kit.kitbot.document.Query;
import com.kit.kitbot.dto.QueryRequestDTO;
import com.kit.kitbot.dto.QueryResponseDTO;
import com.kit.kitbot.dto.SourceDTO;
import com.kit.kitbot.dto.rag.RagResponseDTO;
import com.kit.kitbot.repository.Query.QueryRepository;
import com.kit.kitbot.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {

    private final TranslationService translationService;
    private final QueryRepository queryRepository;
    private final RagWebClient ragWebClient;
    private final UserRepository userRepository;
    private final AnswerKeywordStatService answerKeywordStatService;

    @Transactional
    public QueryResponseDTO processQuestion(QueryRequestDTO requestDTO, String userId) {

        String originalQuestion = requestDTO.getQuestion();
        String appLanguage = requestDTO.getAppLanguage();

        if (appLanguage == null || (!"ko".equals(appLanguage) && !"en".equals(appLanguage))) {
            appLanguage = "ko";
        }

        boolean isKoreanInput = containsKorean(originalQuestion);
        String finalQuestion = originalQuestion;

        if (!isKoreanInput) {
            finalQuestion = translationService.translateText(originalQuestion, "en", "ko");
            log.info("질문 번역됨: {} -> {}", originalQuestion, finalQuestion);
        }

        RagResponseDTO ragResponse;
        try {
            ragResponse = ragWebClient.sendQuestion(finalQuestion);
            log.info("RAG 응답 수신: {}", ragResponse);
        } catch (Exception e) {
            log.error("RAG 서버 통신 실패", e);
            String errorMsg = "en".equals(appLanguage)
                    ? "Sorry, failed to connect to AI server."
                    : "죄송합니다. AI 서버 연결에 실패했습니다.";
            return new QueryResponseDTO(errorMsg, new ArrayList<>(), false, null, null, null);
        }

        List<String> answerKeywords = new ArrayList<>();
        if (ragResponse.getKeyword() != null && !ragResponse.getKeyword().isBlank()) {
            String keyword = ragResponse.getKeyword();
            answerKeywordStatService.increaseByRawKeywords(List.of(keyword));
            answerKeywords.add(keyword);
        }

        String inputLang = isKoreanInput ? "ko" : "en";
        Query toSave = new Query(finalQuestion, inputLang, answerKeywords);
        queryRepository.save(toSave);

        String aiAnswer = ragResponse.getMessage();

        List<String> titles = ragResponse.getSource();
        List<String> links = ragResponse.getLink();
        List<SourceDTO> sources = new ArrayList<>();

        if (titles != null) {
            for (int i = 0; i < titles.size(); i++) {
                String title = titles.get(i);
                String url = (links != null && links.size() > i) ? links.get(i) : "";
                sources.add(new SourceDTO((long) i, title, url));
            }
        }

        String finalAnswer = aiAnswer;
        if ("en".equals(appLanguage)) {
            finalAnswer = translationService.translateText(aiAnswer, "ko", "en");
            log.info("답변 번역됨: {} -> {}", aiAnswer, finalAnswer);
        }

        // 👇 수정: 새 필드 추가
        return new QueryResponseDTO(
                finalAnswer,
                sources,
                ragResponse.isDate(),
                ragResponse.getStartDate(),      // startDate 먼저
                ragResponse.getEndDate(),        // endDate
                ragResponse.getScheduleTitle()   // scheduleTitle 마지막
        );
    }

    private boolean containsKorean(String text) {
        return text.matches(".*[가-힣]+.*");
    }
}