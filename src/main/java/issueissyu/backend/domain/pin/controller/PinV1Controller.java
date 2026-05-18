package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinCommunicationCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pins")
@RequiredArgsConstructor
@Tag(name = "Pin V1", description = "통합 멀티파트 핀 등록 API")
public class PinV1Controller {

    private final PinCommunicationCommandService pinCommunicationCommandService;

    @Operation(
            summary = "소통 핀 통합 등록(V1)",
            description =
                    "photos(이미지 파일들)와 request(JSON: 위/경도, 제목, 본문, 이미지별 isMain)를 한 번에 받아 S3 업로드 후 소통 핀을 등록합니다.")
    @PostMapping(value = "/import/communication", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunicationPinImportResDTO> importCommunicationV1(
            @AuthenticationPrincipal String uid,
            @Valid @RequestPart("request") CommunicationPinImportMultipartReqDTO request,
            @RequestPart("photos") List<MultipartFile> photos) {
        CommunicationPinImportResDTO res = pinCommunicationCommandService.importCommunicationV1(uid, request, photos);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_IMPORT_COMMUNICATION_200, res);
    }
}
