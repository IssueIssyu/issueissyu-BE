package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.res.PinLikeResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinLikeCommandService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pin Like", description = "핀 공감 관련 API")
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinLikeController {

    private final PinLikeCommandService pinLikeCommandService;

    @Operation(summary = "핀 공감", description = "사용자가 핀에 공감합니다. 한 번 공감한 핀에는 다시 요청할 수 없고, 취소는 제공하지 않습니다.")
    @PostMapping("/{pinId}/like")
    public ApiResponse<PinLikeResDTO> likePin(
            @PathVariable Long pinId, @AuthenticationPrincipal String uid) {
        return ApiResponse.onSuccess(PinSuccessCode.PIN_LIKE_200, pinLikeCommandService.likePin(pinId, uid));
    }
}
