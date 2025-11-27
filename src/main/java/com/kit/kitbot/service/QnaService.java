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

    @Transactional
    public QueryResponseDTO processQuestion(QueryRequestDTO requestDTO, String userId) {

        String originalQuestion = requestDTO.getQuestion();

        // 1. [언어 감지]
        String detectedLang = translationService.detectLanguage(originalQuestion);
        String finalQuestion = originalQuestion;

        // 2. [입력 번역] (영어 -> 한글)
        if (!"ko".equalsIgnoreCase(detectedLang)) {
            finalQuestion = translationService.translateText(originalQuestion, detectedLang, "ko");
            log.info("질문 번역됨: {} -> {}", originalQuestion, finalQuestion);
        }

        // 3. [DB 저장] 질문 저장
        Query toSave = new Query(finalQuestion, detectedLang);
        queryRepository.save(toSave);

        // ====================================================
        // 4. [RAG 서버 통신]
        // ====================================================
        RagResponseDTO ragResponse;
        try {
            ragResponse = ragWebClient.sendQuestion(finalQuestion);
            log.info("RAG 응답 수신: {}", ragResponse);
        } catch (Exception e) {
            log.error("RAG 서버 통신 실패", e);
            // 에러 시 isDate=false, 빈 소스 반환
            return new QueryResponseDTO("죄송합니다. AI 서버 연결에 실패했습니다.", new ArrayList<>(), false);
        }

        // 5. [데이터 변환] RAG 응답(분리된 리스트) -> 프론트용 SourceDTO(객체 리스트)로 병합
        String aiAnswer = ragResponse.getMessage(); // answer 대신 message 사용
        List<String> titles = ragResponse.getSource();
        List<String> links = ragResponse.getLink();
        List<SourceDTO> sources = new ArrayList<>();

        // 제목 리스트가 존재하면 루프를 돌며 DTO 생성
        if (titles != null) {
            for (int i = 0; i < titles.size(); i++) {
                String title = titles.get(i);
                // 링크 리스트가 더 짧을 경우를 대비한 안전장치
                String url = (links != null && links.size() > i) ? links.get(i) : "";

                // id는 순서(rank)로 대체
                sources.add(new SourceDTO((long) i, title, url));
            }
        }

        // 6. [출력 번역] (한글 답변 -> 사용자 언어)
        String finalAnswer = aiAnswer;
        if (!"ko".equalsIgnoreCase(detectedLang)) {
            finalAnswer = translationService.translateText(aiAnswer, "ko", detectedLang);
            log.info("답변 번역됨: {} -> {}", aiAnswer, finalAnswer);
        }

        // 7. [알림 발송]
//        if (userId != null) {
//            userRepository.findById(userId).ifPresent(user -> {
//                String pushToken = user.getPushToken();
//                if (pushToken != null) {
//                    notificationService.sendPush(pushToken, "KIT-Bot", finalAnswer);
//                }
//            });
//        }

        // 8. 최종 반환 (isDate 정보 포함)
        // QueryResponseDTO 생성자에 isDate 파라미터가 추가되어 있어야 함
        return new QueryResponseDTO(finalAnswer, sources, ragResponse.isDate());
    }
}