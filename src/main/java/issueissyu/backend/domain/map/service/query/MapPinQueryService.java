package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinClusterResDTO;

public interface MapPinQueryService {

    // BBox(Bounding Box) 내에 위치한 핀 목록을 조회합니다.
    // @param swLng 남서쪽 경도
    // @param swLat 남서쪽 위도
    // @param neLng 북동쪽 경도
    // @param neLat 북동쪽 위도
    MapPinResDTO getPinsInBoundingBox(double swLng, double swLat, double neLng, double neLat, String pinTypeFilter);

    // BBox(Bounding Box) 내 핀을 grid로 스냅해 클러스터링 조회합니다.
    MapPinClusterResDTO getPinClustersInBoundingBox(
            double swLng,
            double swLat,
            double neLng,
            double neLat,
            String pinTypeFilter,
            int zoomLevel
    );
}
