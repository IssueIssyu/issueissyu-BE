package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.dto.res.MapPinClusterResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinClusterView;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapPinQueryServiceImpl implements MapPinQueryService {

    private final PinLocationRepository pinLocationRepository;

    @Override
    public MapPinResDTO getPinsInBoundingBox(double swLng, double swLat, double neLng, double neLat, String pinTypeFilter) {
        try {
            List<MapPinView> views = pinLocationRepository.findPinsInBoundingBox(swLng, swLat, neLng, neLat, pinTypeFilter);
            List<MapPinResDTO.PinItemDTO> pins = views.stream()
                    .map(this::toDto)
                    .toList();
            return new MapPinResDTO(pins);
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.MAP_400_3);
        }
    }

    @Override
    public MapPinClusterResDTO getPinClustersInBoundingBox(
            double swLng,
            double swLat,
            double neLng,
            double neLat,
            String pinTypeFilter,
            int zoomLevel
    ) {
        try {
            double gridSize = resolveGridSize(zoomLevel);
            List<MapPinClusterView> views = pinLocationRepository.findPinClustersInBoundingBox(
                    swLng, swLat, neLng, neLat, pinTypeFilter, gridSize);

            Map<ClusterKey, List<MapPinResDTO.PinItemDTO>> grouped = new LinkedHashMap<>();
            for (MapPinClusterView view : views) {
                Double clusterLat = view.getClusterLat();
                Double clusterLng = view.getClusterLng();
                if (clusterLat == null || clusterLng == null) {
                    continue;
                }
                ClusterKey key = new ClusterKey(clusterLat, clusterLng);
                grouped.computeIfAbsent(key, ignored -> new java.util.ArrayList<>())
                        .add(toDto(view));
            }

            List<MapPinClusterResDTO.ClusterItemDTO> clusters = new java.util.ArrayList<>(grouped.size());
            long clusterId = 1L;
            for (Map.Entry<ClusterKey, List<MapPinResDTO.PinItemDTO>> entry : grouped.entrySet()) {
                ClusterKey key = entry.getKey();
                List<MapPinResDTO.PinItemDTO> pins = entry.getValue();
                clusters.add(new MapPinClusterResDTO.ClusterItemDTO(
                        clusterId++,
                        key.clusterLat(),
                        key.clusterLng(),
                        pins.size(),
                        pins
                ));
            }

            return new MapPinClusterResDTO(clusters);
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.MAP_400_3);
        }
    }

    private MapPinResDTO.PinItemDTO toDto(MapPinView view) {
        return new MapPinResDTO.PinItemDTO(
                view.getPinId(),
                view.getPinType(),
                view.getLat(),
                view.getLng(),
                view.getDetailAddress(),
                view.getRegion()
        );
    }

    private MapPinResDTO.PinItemDTO toDto(MapPinClusterView view) {
        return new MapPinResDTO.PinItemDTO(
                view.getPinId(),
                view.getPinType(),
                view.getLat(),
                view.getLng(),
                view.getDetailAddress(),
                view.getRegion()
        );
    }

    private double resolveGridSize(int zoomLevel) {
        if (zoomLevel <= 4) {
            return 0.182;   // 약 20km
        }
        if (zoomLevel <= 10) {
            return 0.028;   // 약 3km
        }
        // zoomLevel > 10 이어도 클러스터링 API에서 호출될 수 있으므로 더 촘촘한 gridSize를 반환.
        return 0.010;
    }

    private record ClusterKey(double clusterLat, double clusterLng) {
    }
}
