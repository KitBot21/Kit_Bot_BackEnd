package com.kit.kitbot.controller;

import com.kit.kitbot.dto.QueryRequestDTO;
import com.kit.kitbot.dto.QueryResponseDTO;

import com.kit.kitbot.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")  // 추가
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final QnaService qnaService;

    @PostMapping("/query")
    public ResponseEntity<QueryResponseDTO> query(@RequestBody QueryRequestDTO requestDTO) {
        QueryResponseDTO response = qnaService.processQuestion(requestDTO);
        return ResponseEntity.ok(response);
    }

}