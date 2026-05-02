package issueissyu.backend.domain.location.repository;

import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PinLocationRepository extends JpaRepository<PinLocation, Long> {

    // 화면 BBox(Bounding Box) 안에 있는 핀을 조회합니다.
    // PostGIS ST_MakeEnvelope 인자 순서: (Xmin, Ymin, Xmax, Ymax) = (swLng, swLat, neLng, neLat)

    // @param swLng 남서쪽 경도
    // @param swLat 남서쪽 위도
    // @param neLng 북동쪽 경도
    // @param neLat 북동쪽 위도

    // 노출 규칙은 매일 자정 배치(scheduler)에서 pin.visibility_status로 반영된다.
    // 조회 시에는 bbox 내 + visibility_status 가 true 인 핀만 반환한다.
    @Query(value = """
            SELECT
                p.pin_id        AS pinId,
                p.pin_type      AS pinType,
                ST_Y(pl.pin_point) AS lat,
                ST_X(pl.pin_point) AS lng,
                pl.detail_address  AS detailAddress,
                l.location         AS region
            FROM pin_location pl
            INNER JOIN pin      p ON pl.pin_id      = p.pin_id
            INNER JOIN location l ON pl.location_id = l.location_id
            WHERE p.visibility_status = true
              AND ST_Within(
                    pl.pin_point,
                    ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
                  )
            """, nativeQuery = true)
    List<MapPinView> findPinsInBoundingBox(
            @Param("swLng") double swLng,
            @Param("swLat") double swLat,
            @Param("neLng") double neLng,
            @Param("neLat") double neLat
    );
}
