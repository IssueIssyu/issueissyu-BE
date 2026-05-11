package issueissyu.backend.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;
import issueissyu.backend.domain.community.service.command.CommunityCommandService;
import issueissyu.backend.domain.community.service.query.CommunityQueryService;
import issueissyu.backend.domain.pin.dto.req.DeclarationReqDTO;
import issueissyu.backend.domain.pin.service.command.DeclarationCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Community", description = "커뮤니티 피드·상세·삭제 API")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Validated
public class CommunityController {

    private final CommunityQueryService communityQueryService;
    private final CommunityCommandService communityCommandService;
    private final DeclarationCommandService declarationCommandService;

    @Operation(summary = "커뮤니티 피드 조회", description = "탭+지역구+커서 기준으로 피드를 조회합니다.")
    @GetMapping
    public ApiResponse<CommunityCursorPageResDTO> getFeed(
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @RequestParam String region,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        CommunityTab parsedTab = CommunityTab.parse(tab);
        CommunityCursorPageResDTO result =
                communityQueryService.getCommunityFeed(parsedTab, region, cursor, size);
        return ApiResponse.onSuccess(CommunitySuccessCode.forTab(parsedTab), result);
    }

    @Operation(summary = "커뮤니티 게시물 상세 조회", description = "연결된 핀 카드 정보와 본문(content)을 반환합니다. 조회 시 조회수가 증가합니다.")
    @GetMapping("/{communityId}")
    public ApiResponse<CommunityDetailResDTO> getDetail(
            @PathVariable Long communityId,
            @AuthenticationPrincipal String uid) {
        CommunityDetailResDTO result = communityQueryService.getCommunityDetail(communityId, uid);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_DETAIL_200, result);
    }

    @Operation(summary = "커뮤니티 게시물 삭제", description = "소통(COMMUNICATION) 타입만 허용. 작성자만 가능하며 연결된 핀과 모든 하위 데이터(댓글·공감·이모지 등)가 함께 삭제됩니다.")
    @DeleteMapping("/{communityId}")
    public ApiResponse<Void> deleteCommunity(
            @PathVariable Long communityId,
            @AuthenticationPrincipal String uid) {
        communityCommandService.deleteCommunity(communityId, uid);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_DELETE_200, null);
    }

    @Operation(summary = "커뮤니티 게시물 글 내리기", description = "이슈·소통 모두 허용. 작성자만 가능하며 커뮤니티 게시물만 삭제됩니다(핀은 유지).")
    @DeleteMapping("/{communityId}/takedown")
    public ApiResponse<Void> takedownCommunity(
            @PathVariable Long communityId,
            @AuthenticationPrincipal String uid) {
        communityCommandService.takedownCommunity(communityId, uid);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_TAKEDOWN_200, null);
    }

    @Operation(summary = "커뮤니티 게시물 신고", description = "신고 사유 인덱스(1~5)를 받아 핀 단위로 신고를 접수합니다. 동일 게시물 중복 신고 불가.")
    @PostMapping("/{communityId}/declaration")
    public ApiResponse<Void> declareCommunity(
            @PathVariable Long communityId,
            @AuthenticationPrincipal String uid,
            @RequestBody @Valid DeclarationReqDTO request) {
        declarationCommandService.declareCommunity(communityId, uid, request.reasonIndex());
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_DECLARATION_200, null);
    }

}
