package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapPinQueryServiceImpl implements MapPinQueryService {

    private final PinLocationRepository pinLocationRepository;

    @Override
    public MapPinResDTO getPinsInBoundingBox(double swLng, double swLat, double neLng, double neLat) {
        try {
            List<MapPinView> views = pinLocationRepository.findPinsInBoundingBox(swLng, swLat, neLng, neLat);
            List<MapPinResDTO.PinItemDTO> pins = views.stream()
                    .map(this::toDto)
                    .toList();
            return new MapPinResDTO(pins);
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.MAP_400_2);
        }
    }

    private MapPinResDTO.PinItemDTO toDto(MapPinView view) {
        return new MapPinResDTO.PinItemDTO(
                view.getPinId(),
                view.getPinType(),
                view.getLat(),   // pinSw = 위도
                view.getLng(),   // pinNe = 경도
                view.getDetailAddress(),
                view.getRegion()
        );
    }
}
