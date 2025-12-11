package com.kit.kitbot.controller.ChatControllerManage;

import com.kit.kitbot.dto.LatestKeywordQuestionDTO;
import com.kit.kitbot.dto.PopularKeywordDto;
import com.kit.kitbot.service.AnswerKeywordStatService;
import com.kit.kitbot.service.PopularQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/popular")
@Tag(
        name = "실시간 인기 질문 키워드 API",
        description = "RAG 답변에서 추출된 키워드를 기반으로, 오늘 기준 실시간 인기 질문 키워드를 관리/조회하는 API"
)
public class PopularKeywordController {

    private final AnswerKeywordStatService statService;
    private final PopularQuestionService popularQuestionService;

    public PopularKeywordController(AnswerKeywordStatService statService,
                                    PopularQuestionService popularQuestionService) {
        this.statService = statService;
        this.popularQuestionService = popularQuestionService;
    }


    @Operation(
            summary = "키워드 집계 테스트 (개발용)",
            description = """
                    RAG 서버를 거치지 않고도 임의의 키워드를 직접 보내 집계할 수 있는 개발/테스트용 API입니다.
                    
                    - 요청 바디에 문자열 배열 형태로 키워드를 전달하면,
                      각 키워드가 '오늘 날짜' 기준으로 1씩 증가합니다.
                    - 실제 운영에서는 QnaService에서 RAG 응답을 받을 때 자동으로 집계됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "정상적으로 키워드 집계 완료",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/hit")
    public void hit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "집계할 키워드 목록 (문자열 배열)",
                    required = true
            )
            @RequestBody List<String> keywords
    ) {
        statService.increaseByRawKeywords(keywords);
    }


    @Operation(
            summary = "실시간 인기 답변 키워드 Top N 조회",
            description = """
                    오늘 날짜 기준으로, RAG 답변에서 가장 많이 등장한 키워드 상위 N개를 조회합니다.
                    
                    - 기본값 size=5 이며, 최대 원하는 개수만큼 조절 가능합니다.
                    - 응답에는 키워드 문자열과 오늘 기준 누적 카운트가 포함됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "오늘 기준 인기 키워드 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PopularKeywordDto.class))
                    )
            )
    })
    @GetMapping("/answer-keywords")
    public List<PopularKeywordDto> top(
            @Parameter(
                    description = "조회할 키워드 개수 (Top N). 기본값은 5",
                    example = "5"
            )
            @RequestParam(defaultValue = "5") int size
    ) {
        return statService.getTodayTop(size);
    }


    @Operation(
            summary = "키워드별 최신 질문 1개 조회",
            description = """
                    주어진 키워드(answerKeyword)가 포함된 질의 로그 중,
                    가장 최근에 질문된 1개를 조회합니다.
                    
                    - QnaService에서 RAG 응답 키워드를 Query.answerKeywords에 저장해야 동작합니다.
                    - 인기 키워드 목록에서 키워드를 클릭했을 때,
                      해당 키워드와 연관된 최신 질문을 자동으로 다시 질문하는 데 사용할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "해당 키워드로 답변된 최신 질문 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LatestKeywordQuestionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 키워드로 답변된 질문이 아직 없음",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @GetMapping("/answer-keywords/latest-question")
    public ResponseEntity<LatestKeywordQuestionDTO> latestQuestion(
            @RequestParam String keyword
    ) {
        LatestKeywordQuestionDTO dto = popularQuestionService.getLatestQuestionByKeyword(keyword);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
