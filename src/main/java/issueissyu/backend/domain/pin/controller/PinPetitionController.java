package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.issue.dto.res.PetitionStatusResDTO;
import issueissyu.backend.domain.issue.dto.res.PetitionSubmitResDTO;
import issueissyu.backend.domain.issue.exception.code.IssueSuccessCode;
import issueissyu.backend.domain.issue.service.command.IssuePetitionCommandService;
import issueissyu.backend.domain.issue.service.query.IssuePetitionQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Issue Petition", description = "이슈 청원 API")
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinPetitionController {

    private final IssuePetitionCommandService issuePetitionCommandService;
    private final IssuePetitionQueryService issuePetitionQueryService;

    @Operation(summary = "청원하기", description = "이슈 핀에 청원합니다. 동일 핀·사용자에 대해 한 번만 가능합니다.")
    @PostMapping("/{pinId}/petitions")
    public ApiResponse<PetitionSubmitResDTO> submitPetition(
            @PathVariable Long pinId, @AuthenticationPrincipal String uid) {
        return ApiResponse.onSuccess(
                IssueSuccessCode.PETITION_200, issuePetitionCommandService.submitPetition(pinId, uid));
    }

    @Operation(summary = "청원 현황 조회", description = "청원 수·내 청원 여부·목표 청원 수(현재 30 고정)를 반환합니다.")
    @GetMapping("/{pinId}/petitions/status")
    public ApiResponse<PetitionStatusResDTO> getPetitionStatus(
            @PathVariable Long pinId, @AuthenticationPrincipal String uid) {
        return ApiResponse.onSuccess(
                IssueSuccessCode.PETITION_STATUS_200, issuePetitionQueryService.getPetitionStatus(pinId, uid));
    }
}
