package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinImageWithIdResDTO(
        Long pinImageId,
        String pinImageUrl,
        @JsonProperty("isMain") boolean isMain) {}
