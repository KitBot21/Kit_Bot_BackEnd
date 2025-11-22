package com.kit.kitbot.controller.NoticeControllerManage;

import com.kit.kitbot.document.NoticeKeyword;
import com.kit.kitbot.dto.Notice.*;
import com.kit.kitbot.service.NoticeKeywordSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "공지 키워드", description = "공지 키워드 구독/조회 API")
@RestController
@RequestMapping("/api/notice-keywords")
@RequiredArgsConstructor
public class NoticeKeywordController {

    private final NoticeKeywordSubscriptionService subService;

    /**
     * 1) 고정 키워드 전체 조회
     * 프론트 키워드 선택 UI용
     */
    @Operation(summary = "고정 키워드 목록 조회", description = "프로그램에서 미리 정의한 N개(현재 5개)의 공지 키워드 조회")
    @GetMapping
    public List<NoticeKeywordItemDto> getKeywordList() {
        return Stream.of(NoticeKeyword.values())
                .map(NoticeKeywordItemDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 2) 내 구독 목록 조회
     */
    @Operation(summary = "내 구독 키워드 조회", description = "현재 로그인한 사용자가 구독 중인 공지 키워드 목록 조회")
    @GetMapping("/me")
    public List<MyKeywordSubscriptionDto> getMySubscriptions(Principal principal) {

        String userId = principal.getName(); // 나중에 OAuth2 붙으면 자동

        return subService.getMySubscriptions(userId)
                .stream()
                .map(MyKeywordSubscriptionDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 3) 특정 키워드 토글
     * ex) PATCH /api/notice-keywords/SCHOLARSHIP/toggle
     */
    @Operation(summary = "키워드 구독 토글", description = "특정 공지 키워드를 구독/해제 토글, 구독 정보가 없으면 새로 생성")
    @PatchMapping("/{keyword}/toggle")
    public ToggleKeywordResponseDto toggleKeyword(
            @PathVariable String keyword,
            Principal principal
    ) {
        String userId = principal.getName();

        NoticeKeyword kw = NoticeKeyword.valueOf(keyword.toUpperCase());

        return ToggleKeywordResponseDto.from(
                subService.toggle(userId, kw)
        );
    }

    /**
     * 4) (선택) 여러 키워드 일괄 저장
     * enabledKeywords = ["SCHOLARSHIP", "EVENT"] 형식
     */
    @Operation(summary = "구독 키워드 일괄 저장", description = "enabledKeywords에 포함된 키워드만 구독 활성화")
    @PutMapping("/me")
    public SaveMyKeywordsResponseDto saveAll(
            @RequestBody SaveMyKeywordsRequestDto req,
            Principal principal
    ) {
        String userId = principal.getName();

        Set<NoticeKeyword> enabledSet = req.getEnabledKeywords().stream()
                .map(k -> NoticeKeyword.valueOf(k.toUpperCase()))
                .collect(Collectors.toSet());

        var saved = subService.saveAll(userId, enabledSet);

        return SaveMyKeywordsResponseDto.of(
                saved.stream().map(MyKeywordSubscriptionDto::from).toList()
        );
    }
}
