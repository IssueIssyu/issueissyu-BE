package issueissyu.backend.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.pin.dto.res.PinLikeResDTO;
import issueissyu.backend.domain.pin.service.command.PinLikeCommandService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Community Like", description = "커뮤니티 공감 API (핀 공감 로직 공유)")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityLikeController {

    private final CommunityRepository communityRepository;
    private final PinLikeCommandService pinLikeCommandService;

    @Operation(
            summary = "커뮤니티 게시물 공감",
            description =
                    "communityId로 연결된 pin에 공감합니다. 핀 공감과 동일한 데이터(pin_like, like_count)를 사용하며, 취소는 제공하지 않습니다.")
    @PostMapping("/{communityId}/like")
    public ApiResponse<PinLikeResDTO> likeCommunity(
            @PathVariable Long communityId, @AuthenticationPrincipal String uid) {
        Long pinId = resolvePinId(communityId);
        return ApiResponse.onSuccess(
                CommunitySuccessCode.COMMUNITY_LIKE_200, pinLikeCommandService.likePin(pinId, uid));
    }

    // 헬퍼 메서드
    private Long resolvePinId(Long communityId) {
        return communityRepository
                .findById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1))
                .getPin()
                .getPinId();
    }
}
