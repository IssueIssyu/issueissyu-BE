package issueissyu.backend.domain.map.dto.res;

import java.util.List;

public record MapPinResDTO(List<PinItemDTO> pins) {

    public record PinItemDTO(
            Long pinId,
            String pinType,
            double latitude,              // 위도 (latitude)
            double longitude,             // 경도 (longitude)
            String pinDetailAddress,
            String pinLocation
    ) {
    }
}
