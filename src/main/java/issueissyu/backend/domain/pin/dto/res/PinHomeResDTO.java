package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record PinHomeResDTO(
        Long pinId,
        String pinType,
        String pinTitle,
        String pinContent,
        String issuePinState,
        String pinDetailAddress,
        Long likeCount,
        @JsonProperty("isLike") boolean isLike,
        String pinUserId,
        String pinUserProfile,
        String pinUserNickname,
        List<PinImageWithIdResDTO> pinImageUrls,
        String discount,
        String storeImageUrl,
        @JsonProperty("isUpdated") boolean isUpdated,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime updatedAt,
        int viewCount,
        @JsonProperty("isReported") boolean isReported,
        @JsonProperty("isMine") boolean isMine) {}
