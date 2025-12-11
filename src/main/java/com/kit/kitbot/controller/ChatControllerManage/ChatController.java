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
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody QueryRequestDTO requestDTO
    ) {
        String userId = null;

        if (token != null && token.startsWith("Bearer ")) {
            try {
                String actualToken = token.replace("Bearer ", "");
                if (jwtTokenProvider.validateToken(actualToken)) {
                    userId = jwtTokenProvider.getUserIdFromToken(actualToken);
                }
            } catch (Exception e) {
                log.warn("토큰 검증 실패 (게스트로 처리합니다): {}", e.getMessage());
            }
        }


        QueryResponseDTO response = qnaService.processQuestion(requestDTO, userId);

        return ResponseEntity.ok(response);
    }
}