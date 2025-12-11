package com.kit.kitbot.controller;

import com.kit.kitbot.document.User;
import com.kit.kitbot.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "관리자 사용자 관리 API",
        description = "관리자가 사용자 목록을 조회하고, 사용자를 정지/정지 해제할 수 있는 API"
)
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(
            summary = "사용자 목록 조회",
            description = "관리자가 모든 사용자를 페이지 단위로 조회합니다. " +
                    "추후 keyword/role/status 파라미터로 검색 및 필터링을 확장할 수 있습니다."
    )
    public Page<User> getUsers(
            @Parameter(description = "검색 키워드 (이메일/닉네임 등, 추후 확장용)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "역할 필터 (guest, kumoh, admin)")
            @RequestParam(required = false) String role,

            @Parameter(description = "상태 필터 (active, blocked, deleted)")
            @RequestParam(required = false) String status,

            @ParameterObject Pageable pageable
    ) {
        return adminUserService.searchUsers(keyword, role, status, pageable);
    }

    @PatchMapping("/{userId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "사용자 정지",
            description = "지정한 사용자의 상태를 blocked 로 변경합니다."
    )
    public void blockUser(
            @Parameter(description = "정지할 사용자 ID", required = true)
            @PathVariable String userId
    ) {
        adminUserService.blockUser(userId);
    }

    @PatchMapping("/{userId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "사용자 정지 해제",
            description = "지정한 사용자의 상태를 active 로 변경합니다."
    )
    public void activateUser(
            @Parameter(description = "정지 해제할 사용자 ID", required = true)
            @PathVariable String userId
    ) {
        adminUserService.activateUser(userId);
    }
}
