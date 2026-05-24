package issueissyu.backend.domain.pin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.req.StorePinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinEditResDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import issueissyu.backend.domain.pin.dto.res.StorePinImportResDTO;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.service.command.PinCommunicationCommandService;
import issueissyu.backend.domain.pin.service.command.PinStoreCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
@Tag(name = "Pin", description = "핀 등록/수정/삭제/조회")
public class PinV1Controller {

    private final PinCommunicationCommandService pinCommunicationCommandService;
    private final PinStoreCommandService pinStoreCommandService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(
            summary = "소통 핀 통합 등록",
            description =
                    "photos(선택, 이미지 파일들)와 request(JSON: 위/경도, 제목, 본문, 이미지별 isMain)를 받아 필요 시 S3 업로드 후 소통 핀을 등록합니다.")
    @PostMapping(value = "/import/communication", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunicationPinImportResDTO> importCommunication(
            @AuthenticationPrincipal String uid,
            @Parameter(description = "`CommunicationPinImportMultipartReqDTO`와 동일한 필드를 가진 JSON 문자열(lat, lng, pinImages?, pinTitle, pinContent)")
            @RequestPart("request") String requestPart,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        CommunicationPinImportMultipartReqDTO request = parseMultipartRequestBody(requestPart);
        CommunicationPinImportResDTO res = pinCommunicationCommandService.importCommunicationV1(uid, request, photos);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_IMPORT_COMMUNICATION_200, res);
    }

    @Operation(
            summary = "가게 핀 통합 등록",
            description =
                    """
                    ADMIN 권한 계정만 사용 가능합니다.
                    photos(선택)와 request(JSON), storeProfileImage(필수, 이미지 파일 1개)를 받아 S3 업로드 후 가게 핀을 등록합니다.
                    """)
    @PostMapping(value = "/import/store", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StorePinImportResDTO> importStore(
            @AuthenticationPrincipal String uid,
            @Parameter(
                            description =
                                    "`StorePinImportMultipartReqDTO` JSON(lat, lng, pinImages?, pinTitle, pinContent, eventStartTime, eventEndTime, discount)")
                    @RequestPart("request")
                    String requestPart,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @Parameter(description = "가게 프로필 이미지 파일 (1개 필수)")
            @RequestPart("storeProfileImage")
                    MultipartFile storeProfileImage) {
        StorePinImportMultipartReqDTO request = parseStoreMultipartRequestBody(requestPart);
        StorePinImportResDTO res =
                pinStoreCommandService.importStoreV1(uid, request, photos, storeProfileImage);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_IMPORT_STORE_200, res);
    }

    @Operation(
            summary = "소통 핀 통합 수정",
            description =
                    """
                    photos(선택)와 request(JSON: 제목, 본문, 이미지별 isMain)를 받아 필요 시 S3 업로드 후 소통 핀을 수정합니다.
                    위도·경도·region 은 수정할 수 없습니다. pinImages 와 photos 는 인덱스 1:1 대응합니다.
                    """)
    @PatchMapping(value = "/{pinId}/edit/communication", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunicationPinEditResDTO> patchCommunication(
            @AuthenticationPrincipal String uid,
            @PathVariable Long pinId,
            @Parameter(
                            description =
                                    "`CommunicationPinEditMultipartReqDTO` JSON(pinImageUrls?, pinImages?, pinTitle, pinContent)")
                    @RequestPart("request")
                    String requestPart,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        CommunicationPinEditMultipartReqDTO request = parseEditMultipartRequestBody(requestPart);
        CommunicationPinEditResDTO res =
                pinCommunicationCommandService.editCommunicationV1(uid, pinId, request, photos);
        return ApiResponse.onSuccess(PinSuccessCode.PIN_EDIT_COMMUNICATION_200, res);
    }

    // @deprecated PATCH /{pinId}/edit/communication 사용
    @Deprecated
    @PutMapping(value = "/{pinId}/edit/communication", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunicationPinEditResDTO> editCommunication(
            @AuthenticationPrincipal String uid,
            @PathVariable Long pinId,
            @RequestPart("request") String requestPart,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        return patchCommunication(uid, pinId, requestPart, photos);
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

    private StorePinImportMultipartReqDTO parseStoreMultipartRequestBody(String requestPart) {
        if (!StringUtils.hasText(requestPart)) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
        }
        try {
            StorePinImportMultipartReqDTO dto =
                    objectMapper.readValue(requestPart.trim(), StorePinImportMultipartReqDTO.class);
            Set<ConstraintViolation<StorePinImportMultipartReqDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
            }
            return dto;
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
        }
    }

    private CommunicationPinEditMultipartReqDTO parseEditMultipartRequestBody(String requestPart) {
        if (!StringUtils.hasText(requestPart)) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
        }
        try {
            CommunicationPinEditMultipartReqDTO dto =
                    objectMapper.readValue(requestPart.trim(), CommunicationPinEditMultipartReqDTO.class);
            Set<ConstraintViolation<CommunicationPinEditMultipartReqDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
            }
            return dto;
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
        }
    }
}
