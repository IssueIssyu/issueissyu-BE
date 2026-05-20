package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import issueissyu.backend.domain.pin.dto.res.PinImageUploadUrlsResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinCommunicationCommandService;
import issueissyu.backend.domain.pin.service.command.PinImageUploadCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test/pins")
@RequiredArgsConstructor
@Tag(name = "Pin Test", description = "핀 테스트용 API")
public class TestPinController {

    private final PinImageUploadCommandService pinImageUploadCommandService;
    private final PinCommunicationCommandService pinCommunicationCommandService;

    @Operation(summary = "핀 이미지 업로드", description = "multipart photos (최대 5장, 합계 50MB)")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PinImageUploadUrlsResDTO> uploadPinImages(
            @AuthenticationPrincipal String uid,
            @RequestPart("photos") List<MultipartFile> photos) {
        List<String> urls = pinImageUploadCommandService.uploadPinImages(photos == null ? List.of() : photos);
        return ApiResponse.onSuccess(
                PinSuccessCode.PIN_IMAGE_200, new PinImageUploadUrlsResDTO(urls));
    }

    @Operation(summary = "소통 핀 등록 (테스트)", description = "JSON Body 기반 소통 핀 등록")
    @PostMapping("/import/communication")
    public ApiResponse<CommunicationPinImportResDTO> importCommunication(
            @AuthenticationPrincipal String uid, @Valid @RequestBody CommunicationPinImportReqDTO request) {
        CommunicationPinImportResDTO res = pinCommunicationCommandService.importCommunication(uid, request);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_IMPORT_COMMUNICATION_200, res);
    }
}
