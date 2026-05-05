package issueissyu.backend.domain.pin.dto.res;

import lombok.Builder;

@Builder
public record PinLikeResDTO(
        long pinId,
        int pinLikeCount,
        boolean isLike
) {}
