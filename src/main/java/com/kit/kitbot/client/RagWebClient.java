package com.kit.kitbot.client;

import com.kit.kitbot.dto.rag.RagRequestDTO;
import com.kit.kitbot.dto.rag.RagResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Component
public class RagWebClient {

    private final WebClient webClient;

    public RagWebClient(@Value("${rag.server.url}") String ragUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(ragUrl)
                .defaultHeader("ngrok-skip-browser-warning", "true")
                .build();
    }

    public RagResponseDTO sendQuestion(String question) {
        log.info("RAG 서버로 질문 전송: {}", question);

        RagResponseDTO response = webClient.post()
                .uri("/ask")
                .bodyValue(new RagRequestDTO(question, 5))
                .retrieve()
                .bodyToMono(RagResponseDTO.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        log.info("RAG 서버 응답: {}", response);

        return response;
    }
}