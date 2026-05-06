package issueissyu.backend.domain.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;
import issueissyu.backend.domain.community.service.query.CommunityQueryService;
import issueissyu.backend.domain.location.enums.RegionCode;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Tag(name = "Community", description = "커뮤니티 피드 API")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Validated
public class CommunityController {

    private final CommunityQueryService communityQueryService;

    @Operation(summary = "커뮤니티 피드 조회", description = "탭+지역구+커서 기준으로 피드를 조회합니다.")
    @GetMapping
    public ApiResponse<CommunityCursorPageResDTO> getFeed(
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @RequestParam String region,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        CommunityTab parsedTab = CommunityTab.parse(tab);
        RegionCode parsedRegion = parseRegion(region);
        CommunityCursorPageResDTO result =
                communityQueryService.getCommunityFeed(parsedTab, parsedRegion, cursor, size);
        return ApiResponse.onSuccess(CommunitySuccessCode.COMMUNITY_FEED_200, result);
    }

    private static RegionCode parseRegion(String raw) {
        try {
            return RegionCode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_2);
        }
    }
}
