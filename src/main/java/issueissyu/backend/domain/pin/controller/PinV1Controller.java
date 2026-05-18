package issueissyu.backend.domain.pin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinCommunicationCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/v1/pins")
@RequiredArgsConstructor
@Tag(name = "Pin V1", description = "통합 멀티파트 핀 등록 API")
public class PinV1Controller {

    private final PinCommunicationCommandService pinCommunicationCommandService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(
            summary = "소통 핀 통합 등록(V1)",
            description =
                    "photos(선택, 이미지 파일들)와 request(JSON: 위/경도, 제목, 본문, 이미지별 isMain)를 받아 필요 시 S3 업로드 후 소통 핀을 등록합니다.")
    @PostMapping(value = "/import/communication", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunicationPinImportResDTO> importCommunicationV1(
            @AuthenticationPrincipal String uid,
            @Parameter(description = "`CommunicationPinImportMultipartReqDTO`와 동일한 필드를 가진 JSON 문자열(lat, lng, pinImages?, pinTitle, pinContent)")
            @RequestPart("request") String requestPart,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        CommunicationPinImportMultipartReqDTO request = parseMultipartRequestBody(requestPart);
        CommunicationPinImportResDTO res = pinCommunicationCommandService.importCommunicationV1(uid, request, photos);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_IMPORT_COMMUNICATION_200, res);
    }

    private CommunicationPinImportMultipartReqDTO parseMultipartRequestBody(String requestPart) {
        if (!StringUtils.hasText(requestPart)) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);
        }
        try {
            CommunicationPinImportMultipartReqDTO dto =
                    objectMapper.readValue(requestPart.trim(), CommunicationPinImportMultipartReqDTO.class);
            Set<ConstraintViolation<CommunicationPinImportMultipartReqDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);
            }
            return dto;
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);
        }
    }
}
