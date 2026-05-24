package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record StorePinImportResDTO(
        Long pinId,
        String pinType,
        String region,
        String pinDetailAddress,
        List<PinImageWithIdResDTO> pinImageUrls,
        @JsonProperty("storeProfileImage") String storeProfileImage,
        @JsonProperty("toneType") String toneType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime updatedAt) {}
