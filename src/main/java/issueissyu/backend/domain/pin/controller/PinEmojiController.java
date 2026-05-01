package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
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

    @Operation(summary = "핀 반응 목록 조회")
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
            summary = "내 핀 반응 토글(등록/해제)",
            description = "같은 이모지를 다시 요청하면 반응이 해제되고, 다른 이모지를 요청하면 기존 반응은 해제된 뒤 새 반응으로 교체됩니다."
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
