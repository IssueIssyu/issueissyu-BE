package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.ApplyPinEmojiReqDTO;
import issueissyu.backend.domain.pin.dto.res.ApplyPinEmojiResDTO;
import issueissyu.backend.domain.pin.dto.res.PinEmojiSummaryResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinEmojiCommandService;
import issueissyu.backend.domain.pin.service.query.PinEmojiQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pin Emoji", description = "핀 반응 관련 API")
@RestController
@RequestMapping("/api/pins/{pinId}/emojis")
@RequiredArgsConstructor
public class PinEmojiController {
    private final PinEmojiCommandService pinEmojiCommandService;
    private final PinEmojiQueryService pinEmojiQueryService;

    @Operation(summary = "핀 반응 목록 조회")
    @GetMapping
    public ApiResponse<List<PinEmojiSummaryResDTO>> getPinEmojis(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.PIN_EMOJIS_200,
                pinEmojiQueryService.getPinEmojiSummaries(pinId, uid)
        );
    }

    @Operation(summary = "내 핀 반응 등록")
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

    @Operation(summary = "내 핀 반응 취소")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMyEmoji(
            @PathVariable Long pinId,
            @AuthenticationPrincipal String uid
    ) {
        pinEmojiCommandService.deleteMyEmoji(pinId, uid);
        return ApiResponse.onSuccess(PinSuccessCode.DELETE_EMOJI_200, null);
    }
}
