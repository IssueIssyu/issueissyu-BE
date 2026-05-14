package issueissyu.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.user.dto.res.UserMyPinsResDTO;
import issueissyu.backend.domain.user.exception.code.UserSuccessCode;
import issueissyu.backend.domain.user.service.query.UserPinQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserPinQueryService userPinQueryService;

    @Operation(
            summary = "내 핀 조회",
            description = "인증된 사용자가 생성한 핀을 created_at 내림차순으로 커서 페이징 조회합니다.")
    @GetMapping("/me/pins")
    public ApiResponse<UserMyPinsResDTO> getMyPins(
            @AuthenticationPrincipal String uid,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        return ApiResponse.onSuccess(UserSuccessCode.USER_PIN_200, userPinQueryService.getMyPins(uid, size, cursor));
    }
}
