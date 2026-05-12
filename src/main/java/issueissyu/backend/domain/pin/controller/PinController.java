package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportReqDTO;
import issueissyu.backend.domain.pin.dto.req.DeclarationReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinEditResDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import issueissyu.backend.domain.pin.dto.res.PinHomeResDTO;
import issueissyu.backend.domain.pin.dto.res.PinImageUploadUrlsResDTO;
import issueissyu.backend.domain.pin.dto.res.PinPostResDTO;
import issueissyu.backend.domain.pin.dto.res.PinSolveResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.DeclarationCommandService;
import issueissyu.backend.domain.pin.service.command.PinCommunicationCommandService;
import issueissyu.backend.domain.pin.service.command.PinDeleteCommandService;
import issueissyu.backend.domain.pin.service.command.PinImageUploadCommandService;
import issueissyu.backend.domain.pin.service.query.PinDetailQueryService;
import issueissyu.backend.domain.pin.service.query.PinSolveQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
@Tag(name = "Pin", description = "핀 등록/수정/삭제/조회")
public class PinController {

    private final PinImageUploadCommandService pinImageUploadCommandService;
    private final PinCommunicationCommandService pinCommunicationCommandService;
    private final DeclarationCommandService declarationCommandService;
    private final PinDeleteCommandService pinDeleteCommandService;
    private final PinDetailQueryService pinDetailQueryService;
    private final PinSolveQueryService pinSolveQueryService;

    @Operation(summary = "핀 이미지 업로드", description = "multipart photos (최대 5장, 합계 50MB)")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PinImageUploadUrlsResDTO> uploadPinImages(
            @AuthenticationPrincipal String uid,
            @RequestPart("photos") List<MultipartFile> photos) {
        List<String> urls = pinImageUploadCommandService.uploadPinImages(photos == null ? List.of() : photos);
        return ApiResponse.onSuccess(
                PinSuccessCode.PIN_IMAGE_200, new PinImageUploadUrlsResDTO(urls));
    }

    @Operation(summary = "소통 핀 등록")
    @PostMapping("/import/communication")
    public ApiResponse<CommunicationPinImportResDTO> importCommunication(
            @AuthenticationPrincipal String uid, @Valid @RequestBody CommunicationPinImportReqDTO request) {
        CommunicationPinImportResDTO res = pinCommunicationCommandService.importCommunication(uid, request);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_IMPORT_COMMUNICATION_200, res);
    }

    @Operation(summary = "소통 핀 수정")
    @PutMapping("/{pinId}/edit/communication")
    public ApiResponse<CommunicationPinEditResDTO> editCommunication(
            @AuthenticationPrincipal String uid,
            @PathVariable Long pinId,
            @Valid @RequestBody CommunicationPinEditReqDTO request) {
        CommunicationPinEditResDTO res = pinCommunicationCommandService.editCommunication(uid, pinId, request);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_EDIT_COMMUNICATION_200, res);
    }

    @Operation(summary = "핀 삭제")
    @DeleteMapping("/{pinId}/delete")
    public ApiResponse<Void> deletePin(@AuthenticationPrincipal String uid, @PathVariable Long pinId) {
        pinDeleteCommandService.deletePin(uid, pinId);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_DELETE_200, null);
    }

    @Operation(summary = "핀 신고", description = "신고 사유 인덱스 1~5. 동일 핀에 대한 사용자별 중복 신고 불가. declaration_reason에 인덱스가 저장됩니다.")
    @PostMapping("/{pinId}/declarations")
    public ApiResponse<Void> declarePin(
            @AuthenticationPrincipal String uid,
            @PathVariable Long pinId,
            @Valid @RequestBody DeclarationReqDTO request) {
        declarationCommandService.declarePin(pinId, uid, request.reasonIndex());
        return ApiResponse.onSuccess(PinSuccessCode.PIN_DECLARATION_200, null);
    }

    @Operation(summary = "핀 상세 홈", description = "필수 쿼리 pinId. 조회 시 viewCount(조회수) 중복 집계.")
    @GetMapping("/home")
    public ApiResponse<PinHomeResDTO> getPinHome(
            @AuthenticationPrincipal String uid, @RequestParam Long pinId) {
        PinDetailQueryService.PinHomeResult r = pinDetailQueryService.getPinHome(pinId, uid);
        return ApiResponse.onSuccess(r.successCode(), r.data());
    }

    @Operation(summary = "핀 상세 포스트")
    @GetMapping("/{pinId}/post")
    public ApiResponse<PinPostResDTO> getPinPost(@AuthenticationPrincipal String uid, @PathVariable Long pinId) {
        PinDetailQueryService.PinPostResult r = pinDetailQueryService.getPinPost(pinId, uid);
        return ApiResponse.onSuccess(r.successCode(), r.data());
    }

    @Operation(
            summary = "핀 상세 해결하기 조회",
            description = "이슈 핀만 허용. isPetitioned: 청원 여부, isProblemSolver: 지금가요(시민해결사) 참여 여부")
    @GetMapping("/{pinId}/solve")
    public ApiResponse<PinSolveResDTO> getPinSolve(@AuthenticationPrincipal String uid, @PathVariable Long pinId) {
        PinSolveResDTO data = pinSolveQueryService.getPinSolve(pinId, uid);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_SOLVE_200, data);
    }
}
