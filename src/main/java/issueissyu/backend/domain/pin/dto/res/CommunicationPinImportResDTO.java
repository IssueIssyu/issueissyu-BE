package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record CommunicationPinImportResDTO(
        Long pinId,
        String pinType,
        String pinDetailAddress,
        List<PinImageWithIdResDTO> pinImageUrls,
        @JsonProperty("tone_type") String toneType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime updatedAt) {}
