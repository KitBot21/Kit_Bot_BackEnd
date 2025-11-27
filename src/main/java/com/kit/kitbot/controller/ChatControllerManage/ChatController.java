package com.kit.kitbot.controller.ChatControllerManage;

import com.kit.kitbot.dto.QueryRequestDTO;
import com.kit.kitbot.dto.QueryResponseDTO;
import com.kit.kitbot.security.JwtTokenProvider;
import com.kit.kitbot.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final QnaService qnaService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/query")
    public ResponseEntity<QueryResponseDTO> query(
            // 👇 [핵심] required = false로 설정하여 토큰이 없어도 요청을 받아줍니다.
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody QueryRequestDTO requestDTO
    ) {
        String userId = null;

        // 1. 토큰이 있는 경우에만 해석 (로그인 사용자)
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String actualToken = token.replace("Bearer ", "");
                if (jwtTokenProvider.validateToken(actualToken)) {
                    userId = jwtTokenProvider.getUserIdFromToken(actualToken);
                }
            } catch (Exception e) {
                log.warn("토큰 검증 실패 (게스트로 처리합니다): {}", e.getMessage());
                // 토큰이 이상해도 에러 내지 않고 그냥 userId = null (게스트)로 진행
            }
        }

        // 2. 서비스 호출
        // userId가 있으면 -> 알림 발송 O
        // userId가 없으면(null) -> 알림 발송 X (채팅은 정상 작동)
        QueryResponseDTO response = qnaService.processQuestion(requestDTO, userId);

        return ResponseEntity.ok(response);
    }
}