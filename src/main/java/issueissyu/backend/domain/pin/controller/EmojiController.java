package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.res.EmojiCandidateResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.query.PinEmojiQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Emoji", description = "이모지 피커 API")
@RestController
@RequestMapping("/api/emojis")
@RequiredArgsConstructor
public class EmojiController {

    private final PinEmojiQueryService pinEmojiQueryService;

    @Operation(
            summary = "이모지 피커 목록 조회",
            description = "isDefault=true 또는 isOwned=true면 바로 반응 가능. " +
                    "둘 다 false면 productId로 구글 플레이 구매 후 사용 가능."
    )
    @GetMapping("/candidates")
    public ApiResponse<List<EmojiCandidateResDTO>> getEmojiCandidates(
            @AuthenticationPrincipal String uid
    ) {
        return ApiResponse.onSuccess(
                PinSuccessCode.EMOJI_CANDIDATES_200,
                pinEmojiQueryService.getEmojiCandidates(uid)
        );
    }
}
