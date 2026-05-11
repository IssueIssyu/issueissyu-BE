package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinPostResDTO(
        Long pinId,
        String pinType,
        String pinTitle,
        Long likeCount,
        @JsonProperty("isLike") boolean isLike,
        String pinUserId,
        String pinUserProfile,
        String pinUserNickname,
        String mainPinImageUrl,
        String discount,
        String storeImageUrl) {}
