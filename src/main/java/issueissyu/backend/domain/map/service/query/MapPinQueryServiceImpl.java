package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.cache.PinGeoRedisService;
import issueissyu.backend.domain.map.dto.res.MapPinClusterResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinClusterView;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapPinQueryServiceImpl implements MapPinQueryService {

    private final PinLocationRepository pinLocationRepository;
    private final PinGeoRedisService pinGeoRedisService;

    // ─────────────────────────────────────────────────────────────────
    // 화면 핀 조회 (Cache-Aside)
    // ─────────────────────────────────────────────────────────────────

    @Override
    public MapPinResDTO getPinsInBoundingBox(
            double swLng, double swLat, double neLng, double neLat, String pinTypeFilter) {
        // 1. Redis 캐시 시도
        if (pinGeoRedisService.isGeoSetReady()) {
            Optional<List<MapPinResDTO.PinItemDTO>> cached =
                    pinGeoRedisService.searchByBBox(swLng, swLat, neLng, neLat, pinTypeFilter);
            if (cached.isPresent()) {
                return new MapPinResDTO(cached.get());
            }
        }

        // 2. 캐시 미스 → DB 폴백
        try {
            List<MapPinView> views = pinLocationRepository.findPinsInBoundingBox(
                    swLng, swLat, neLng, neLat, pinTypeFilter);
            List<MapPinResDTO.PinItemDTO> pins = views.stream()
                    .map(this::toDto)
                    .toList();
            return new MapPinResDTO(pins);
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.MAP_400_3);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 클러스터링 핀 조회 (Cache-Aside + Java-side 클러스터링)
    // ─────────────────────────────────────────────────────────────────

    @Override
    public MapPinClusterResDTO getPinClustersInBoundingBox(
            double swLng, double swLat, double neLng, double neLat,
            String pinTypeFilter, int zoomLevel) {

        double gridSize = resolveGridSize(zoomLevel);

        // 1. Redis 캐시 시도 → Java-side 클러스터링
        if (pinGeoRedisService.isGeoSetReady()) {
            Optional<List<MapPinResDTO.PinItemDTO>> cached =
                    pinGeoRedisService.searchByBBox(swLng, swLat, neLng, neLat, pinTypeFilter);
            if (cached.isPresent()) {
                return clusterPins(cached.get(), gridSize);
            }
        }

        // 2. 캐시 미스 → DB 폴백 (PostGIS ST_SnapToGrid 사용)
        try {
            List<MapPinClusterView> views = pinLocationRepository.findPinClustersInBoundingBox(
                    swLng, swLat, neLng, neLat, pinTypeFilter, gridSize);
            return buildClusterDtoFromViews(views);
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.MAP_400_3);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ─────────────────────────────────────────────────────────────────

    // Redis에서 가져온 핀 목록을 PostGIS ST_SnapToGrid 와 동일한 방식으로 클러스터링합니다.
    // snappedLat = round(lat / gridSize) * gridSize
    private MapPinClusterResDTO clusterPins(List<MapPinResDTO.PinItemDTO> pins, double gridSize) {
        Map<ClusterKey, List<MapPinResDTO.PinItemDTO>> grouped = new LinkedHashMap<>();
        for (MapPinResDTO.PinItemDTO pin : pins) {
            double snappedLat = Math.round(pin.latitude() / gridSize) * gridSize;
            double snappedLng = Math.round(pin.longitude() / gridSize) * gridSize;
            grouped.computeIfAbsent(new ClusterKey(snappedLat, snappedLng), k -> new ArrayList<>())
                    .add(pin);
        }
        return buildClusterDto(grouped);
    }

    private MapPinClusterResDTO buildClusterDtoFromViews(List<MapPinClusterView> views) {
        Map<ClusterKey, List<MapPinResDTO.PinItemDTO>> grouped = new LinkedHashMap<>();
        for (MapPinClusterView view : views) {
            Double clusterLat = view.getClusterLat();
            Double clusterLng = view.getClusterLng();
            if (clusterLat == null || clusterLng == null) continue;
            grouped.computeIfAbsent(new ClusterKey(clusterLat, clusterLng), k -> new ArrayList<>())
                    .add(toDto(view));
        }
        return buildClusterDto(grouped);
    }

    private MapPinClusterResDTO buildClusterDto(
            Map<ClusterKey, List<MapPinResDTO.PinItemDTO>> grouped) {
        List<MapPinClusterResDTO.ClusterItemDTO> clusters = new ArrayList<>(grouped.size());
        long clusterId = 1L;
        for (Map.Entry<ClusterKey, List<MapPinResDTO.PinItemDTO>> entry : grouped.entrySet()) {
            ClusterKey key = entry.getKey();
            List<MapPinResDTO.PinItemDTO> pinList = entry.getValue();
            clusters.add(new MapPinClusterResDTO.ClusterItemDTO(
                    clusterId++,
                    key.clusterLat(),
                    key.clusterLng(),
                    pinList.size(),
                    pinList
            ));
        }
        return new MapPinClusterResDTO(clusters);
    }

    private MapPinResDTO.PinItemDTO toDto(MapPinView view) {
        return new MapPinResDTO.PinItemDTO(
                view.getPinId(),
                view.getPinType(),
                view.getLat(),
                view.getLng(),
                view.getDetailAddress(),
                view.getRegion(),
                view.getDiscount()
        );
    }

    private MapPinResDTO.PinItemDTO toDto(MapPinClusterView view) {
        return new MapPinResDTO.PinItemDTO(
                view.getPinId(),
                view.getPinType(),
                view.getLat(),
                view.getLng(),
                view.getDetailAddress(),
                view.getRegion(),
                view.getDiscount()
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
