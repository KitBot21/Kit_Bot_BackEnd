package com.kit.kitbot.controller.ChatControllerManage;

import com.kit.kitbot.dto.PopularKeywordDto;
import com.kit.kitbot.service.AnswerKeywordStatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/popular")
public class PopularKeywordController {

    private final AnswerKeywordStatService statService;

    public PopularKeywordController(AnswerKeywordStatService statService) {
        this.statService = statService;
    }

    /**
     * 🔹 (개발/테스트용) 임의 키워드들 카운트 올리기
     * body 예: ["수강신청", "수강신청 일정", "기숙사 환불"]
     */
    @PostMapping("/hit")
    public void hit(@RequestBody List<String> keywords) {
        statService.increaseByRawKeywords(keywords);
    }

    /**
     * 🔹 오늘 기준 실시간 답변 키워드 Top N 조회
     */
    @GetMapping("/answer-keywords")
    public List<PopularKeywordDto> top(
            @RequestParam(defaultValue = "5") int size
    ) {
        return statService.getTodayTop(size);
    }
}