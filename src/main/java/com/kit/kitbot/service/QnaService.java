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
    //    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 🔹 실시간 인기 키워드 집계 서비스
    private final AnswerKeywordStatService answerKeywordStatService;

    /**
     * Q&A 메인 로직
     *
     * 1. 언어 감지
     * 2. (필요 시) 질문 → 한국어 번역
     * 3. RAG 서버 호출
     * 4. RAG가 돌려준 키워드로 인기 키워드 집계 + Query 컬렉션에 질문/키워드 저장
     * 5. RAG 응답을 SourceDTO 리스트로 변환
     * 6. (필요 시) 답변을 사용자 원어로 다시 번역
     * 7. 최종 QueryResponseDTO 반환
     */
    @Transactional
    public QueryResponseDTO processQuestion(QueryRequestDTO requestDTO, String userId) {

        String originalQuestion = requestDTO.getQuestion();

        // 1. [언어 감지]
        String detectedLang = translationService.detectLanguage(originalQuestion);
        String finalQuestion = originalQuestion;

        // 2. [입력 번역] (사용자 언어 -> 한글)
        if (!"ko".equalsIgnoreCase(detectedLang)) {
            finalQuestion = translationService.translateText(originalQuestion, detectedLang, "ko");
            log.info("질문 번역됨: {} -> {}", originalQuestion, finalQuestion);
        }

        // ====================================================
        // 3. [RAG 서버 통신]
        // ====================================================
        RagResponseDTO ragResponse;
        try {
            ragResponse = ragWebClient.sendQuestion(finalQuestion);
            log.info("RAG 응답 수신: {}", ragResponse);
        } catch (Exception e) {
            log.error("RAG 서버 통신 실패", e);
            // 에러 시 isDate=false, 빈 소스 반환
            return new QueryResponseDTO(
                    "죄송합니다. AI 서버 연결에 실패했습니다.",
                    new ArrayList<>(),
                    false
            );
        }

        // 🔹 3-1. [실시간 인기 키워드 집계 + 이번 질문에 대한 답변 키워드 수집]
        List<String> answerKeywords = new ArrayList<>();
        if (ragResponse.getKeyword() != null && !ragResponse.getKeyword().isBlank()) {
            String keyword = ragResponse.getKeyword();

            // Redis 집계 (실시간 인기 키워드용)
            answerKeywordStatService.increaseByRawKeywords(List.of(keyword));

            // Mongo Query 도큐먼트에 저장할 키워드 리스트
            answerKeywords.add(keyword);
        }

        // 🔹 3-2. [질문 로그 저장]
        //  - question : RAG에 실제로 들어간 한국어 질문(finalQuestion)
        //  - lang     : 사용자가 입력한 원래 언어
        //  - answerKeywords : 이번 답변에 사용된 키워드들
        Query toSave = new Query(finalQuestion, detectedLang, answerKeywords);
        queryRepository.save(toSave);

        // ====================================================
        // 4. [데이터 변환] RAG 응답 -> 프론트 응답 DTO
        // ====================================================
        String aiAnswer = ragResponse.getMessage(); // answer 대신 message 사용

        List<String> titles = ragResponse.getSource();
        List<String> links = ragResponse.getLink();
        List<SourceDTO> sources = new ArrayList<>();

        if (titles != null) {
            for (int i = 0; i < titles.size(); i++) {
                String title = titles.get(i);
                String url = (links != null && links.size() > i) ? links.get(i) : "";
                // id는 단순히 rank 용도로 i 사용
                sources.add(new SourceDTO((long) i, title, url));
            }
        }

        // ====================================================
        // 5. [출력 번역] (한국어 답변 -> 사용자 언어)
        // ====================================================
        String finalAnswer = aiAnswer;
        if (!"ko".equalsIgnoreCase(detectedLang)) {
            finalAnswer = translationService.translateText(aiAnswer, "ko", detectedLang);
            log.info("답변 번역됨: {} -> {}", aiAnswer, finalAnswer);
        }

        // ====================================================
        // 6. [알림 발송] (푸시 기능 붙일 때 주석 해제)
        // ====================================================
//        if (userId != null) {
//            userRepository.findById(userId).ifPresent(user -> {
//                String pushToken = user.getPushToken();
//                if (pushToken != null) {
//                    notificationService.sendPush(pushToken, "KIT-Bot", finalAnswer);
//                }
//            });
//        }

        // ====================================================
        // 7. 최종 반환 (isDate 정보 포함)
        // ====================================================
        return new QueryResponseDTO(finalAnswer, sources, ragResponse.isDate());
    }
}
