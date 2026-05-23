package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.req.RegisterPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.res.ApplyPinEmojiResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryListResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinEmojiCommandService;
import issueissyu.backend.domain.pin.service.query.PinEmojiQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pin Emoji", description = "핀 반응 관련 API")
@RestController
@RequestMapping("/api/pins/{pinId}/emojis")
@RequiredArgsConstructor
public class PinEmojiController {
    private final PinEmojiCommandService pinEmojiCommandService;
    private final PinEmojiQueryService pinEmojiQueryService;

    @Operation(
        summary = "핀 반응 목록 조회",
        description = "selectedEmojiId와 함께, 기본 이모지는 항상 노출하고 비기본 이모지는 해당 핀에 반응(count>0)이 있는 경우만 반응 수/소유 여부/상품 ID를 반환합니다."
    )
    @GetMapping
    public ApiResponse<PinEmojiSummaryListResDTO> getPinEmojis(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.PIN_EMOJIS_200,
                pinEmojiQueryService.getPinEmojiSummaries(pinId, uid)
        );
    }

    @Operation(
            summary = "내 핀 반응 등록(피커 확정)",
            description = "피커에서 선택 후 버튼으로 확정할 때 사용합니다. emojiId=null이면 반응 취소, "
                    + "값이 있으면 등록/변경합니다(같은 이모지 재전송 시 유지, 토글 해제 없음)."
    )
    @PostMapping("/me")
    public ApiResponse<ApplyPinEmojiResDTO> registerMyEmoji(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid,
            @RequestBody RegisterPinEmojiReqDTO request
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.APPLY_EMOJI_200,
                pinEmojiCommandService.registerMyEmoji(pinId, uid, request)
        );
    }

    @Operation(
            summary = "내 핀 반응 토글",
            description = "이미 남긴 반응을 즉시 조작할 때 사용합니다. 같은 이모지를 다시 요청하면 해제되고, "
                    + "다른 이모지를 요청하면 교체됩니다."
    )
    @PutMapping("/me")
    public ApiResponse<ApplyPinEmojiResDTO> applyMyEmoji(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody ApplyPinEmojiReqDTO request
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.APPLY_EMOJI_200,
                pinEmojiCommandService.applyMyEmoji(pinId, uid, request)
        );
    }
}
