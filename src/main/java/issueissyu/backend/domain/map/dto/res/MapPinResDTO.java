package issueissyu.backend.domain.map.dto.res;

import java.util.List;

public record MapPinResDTO(List<PinItemDTO> pins) {

    public record PinItemDTO(
            Long pinId,
            String pinType,
            double pinSw,              // 위도 (latitude)
            double pinNe,              // 경도 (longitude)
            String pinDetailAddress,
            String pinLocation
    ) {
    }
}
