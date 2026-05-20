package issueissyu.backend.domain.pin.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CommunicationPinEditMultipartReqDTO(
        // 유지할 기존 이미지(URL + isMain). null 이면 photos 도 없을 때 이미지 변경 없음
        @Valid @Size(max = 5) List<PinImageItemReqDTO> pinImageUrls,
        // 새로 업로드할 photos 와 1:1 대응하는 isMain. null 이면 빈 목록
        @Valid @Size(max = 5) List<CommunicationPinImportMultipartImageReqDTO> pinImages,
        @NotBlank String pinTitle,
        @NotBlank String pinContent) {}
