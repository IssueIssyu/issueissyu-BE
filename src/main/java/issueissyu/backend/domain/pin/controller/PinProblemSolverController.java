package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverCheckResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverListResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverPhotoResDTO;
import issueissyu.backend.domain.issue.exception.code.IssueSuccessCode;
import issueissyu.backend.domain.issue.service.command.ProblemSolverCommandService;
import issueissyu.backend.domain.issue.service.query.ProblemSolverQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Issue Problem Solver", description = "시민해결사 API")
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinProblemSolverController {

    private final ProblemSolverQueryService problemSolverQueryService;
    private final ProblemSolverCommandService problemSolverCommandService;

    @Operation(
            summary = "시민해결사 목록 조회",
            description = "path의 userId는 회원 UID(문자열)입니다. 명세 예시 숫자 ID와 무관하게 DB uid와 동일해야 합니다.")
    @GetMapping("/{pinId}/{userUid}")
    public ApiResponse<ProblemSolverListResDTO> listProblemSolvers(
            @PathVariable Long pinId,
            @PathVariable String userUid,
            @AuthenticationPrincipal String uid) {
        var envelope = problemSolverQueryService.findProblemSolverList(pinId, userUid, uid);
        return ApiResponse.onSuccess(envelope.successCode(), envelope.body());
    }

    @Operation(summary = "시민해결사 인증 사진 첨부", description = "multipart photo 파트 이름은 photo 입니다.")
    @PostMapping(
            value = "/{problemSolverId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProblemSolverPhotoResDTO> uploadSolverPhoto(
            @PathVariable Long problemSolverId,
            @AuthenticationPrincipal String uid,
            @RequestPart("photo") MultipartFile photo) {
        return ApiResponse.onSuccess(
                IssueSuccessCode.PROBLEM_SOLVER_PHOTO_200,
                problemSolverCommandService.attachVerificationPhoto(problemSolverId, uid, photo));
    }

    @Operation(summary = "내 핀 시민해결사 인증 완료", description = "VERIFIED 상태를 RESOLVED로 변경합니다.")
    @PatchMapping("/{problemSolverId}")
    public ApiResponse<ProblemSolverCheckResDTO> verifyCitizenSolver(
            @PathVariable Long problemSolverId, @AuthenticationPrincipal String uid) {
        return ApiResponse.onSuccess(
                IssueSuccessCode.PROBLEM_SOLVER_CHECK_200,
                problemSolverCommandService.resolveCitizenVerification(problemSolverId, uid));
    }
}
