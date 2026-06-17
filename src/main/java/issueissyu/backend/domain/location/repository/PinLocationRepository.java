package issueissyu.backend.domain.location.repository;

import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.map.dto.res.MapPinClusterView;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PinLocationRepository extends JpaRepository<PinLocation, Long> {

    Optional<PinLocation> findFirstByPin_PinIdOrderByPinLocationIdAsc(Long pinId);

    Optional<PinLocation> findByPin_PinId(Long pinId);

    List<PinLocation> findByPin_PinIdIn(Collection<Long> pinIds);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PinLocation pl WHERE pl.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);

    // 화면 BBox(Bounding Box) 안에 있는 핀을 조회합니다.
    // PostGIS ST_MakeEnvelope 인자 순서: (Xmin, Ymin, Xmax, Ymax) = (swLng, swLat, neLng, neLat)

    // @param swLng 남서쪽 경도
    // @param swLat 남서쪽 위도
    // @param neLng 북동쪽 경도
    // @param neLat 북동쪽 위도

    // 노출: 등록 1년 이내, 소통 핀은 communication_pin.updated_at 기준 1개월 이내 반응, 이벤트 핀은 게시 기간.
    @Query(value = """
            SELECT
                p.pin_id        AS pinId,
                p.pin_type      AS pinType,
                ST_Y(pl.pin_point) AS lat,
                ST_X(pl.pin_point) AS lng,
                pl.detail_address  AS detailAddress,
                l.location         AS region,
                CASE WHEN p.pin_type = 'STORE' THEN ep.discount ELSE NULL END AS discount
            FROM pin_location pl
            INNER JOIN pin      p ON pl.pin_id      = p.pin_id
            INNER JOIN location l ON pl.location_id = l.location_id
            LEFT JOIN communication_pin cp ON cp.pin_id = p.pin_id
            LEFT JOIN event_pin ep ON ep.pin_id = p.pin_id
            WHERE p.created_at >= (NOW() AT TIME ZONE 'Asia/Seoul') - INTERVAL '1 year'
              AND ST_Within(
                    pl.pin_point,
                    ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
                  )
              AND (
                    p.pin_type = 'ISSUE'
                    OR (p.pin_type = 'COMMUNICATION'
                        AND cp.communication_pin_id IS NOT NULL
                        AND COALESCE(cp.updated_at, cp.created_at) >= (NOW() AT TIME ZONE 'Asia/Seoul') - INTERVAL '1 month')
                    OR (p.pin_type IN ('STORE', 'FESTIVAL')
                        AND ep.event_pin_id IS NOT NULL
                        AND (NOW() AT TIME ZONE 'Asia/Seoul') BETWEEN ep.event_start_time AND ep.event_end_time)
                  )
              AND (:pinTypeFilter IS NULL OR p.pin_type = :pinTypeFilter)
            """, nativeQuery = true)
    List<MapPinView> findPinsInBoundingBox(
            @Param("swLng") double swLng,
            @Param("swLat") double swLat,
            @Param("neLng") double neLng,
            @Param("neLat") double neLat,
            @Param("pinTypeFilter") String pinTypeFilter
    );

    @Query(value = """
            SELECT
                p.pin_id AS pinId,
                p.pin_type AS pinType,
                ST_Y(pl.pin_point) AS lat,
                ST_X(pl.pin_point) AS lng,
                pl.detail_address AS detailAddress,
                l.location AS region,
                ST_Y(ST_SnapToGrid(pl.pin_point, :gridSize)) AS clusterLat,
                ST_X(ST_SnapToGrid(pl.pin_point, :gridSize)) AS clusterLng,
                CASE WHEN p.pin_type = 'STORE' THEN ep.discount ELSE NULL END AS discount
            FROM pin_location pl
            INNER JOIN pin      p ON pl.pin_id      = p.pin_id
            INNER JOIN location l ON pl.location_id = l.location_id
            LEFT JOIN communication_pin cp ON cp.pin_id = p.pin_id
            LEFT JOIN event_pin ep ON ep.pin_id = p.pin_id
            WHERE p.created_at >= (NOW() AT TIME ZONE 'Asia/Seoul') - INTERVAL '1 year'
              AND ST_Within(
                    pl.pin_point,
                    ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
                  )
              AND (
                    p.pin_type = 'ISSUE'
                    OR (p.pin_type = 'COMMUNICATION'
                        AND cp.communication_pin_id IS NOT NULL
                        AND COALESCE(cp.updated_at, cp.created_at) >= (NOW() AT TIME ZONE 'Asia/Seoul') - INTERVAL '1 month')
                    OR (p.pin_type IN ('STORE', 'FESTIVAL')
                        AND ep.event_pin_id IS NOT NULL
                        AND (NOW() AT TIME ZONE 'Asia/Seoul') BETWEEN ep.event_start_time AND ep.event_end_time)
                  )
              AND (:pinTypeFilter IS NULL OR p.pin_type = :pinTypeFilter)
            ORDER BY clusterLat, clusterLng, p.pin_id
            """, nativeQuery = true)
    List<MapPinClusterView> findPinClustersInBoundingBox(
            @Param("swLng") double swLng,
            @Param("swLat") double swLat,
            @Param("neLng") double neLng,
            @Param("neLat") double neLat,
            @Param("pinTypeFilter") String pinTypeFilter,
            @Param("gridSize") double gridSize
    );

    Optional<PinLocation> findFirstByPin_PinId(Long pinId);

    // Redis GEO 초기 적재 / 야간 재적재용: BBox 제약 없이 현재 활성 핀을 모두 조회합니다.
    // 활성 조건은 findPinsInBoundingBox 와 동일 (ISSUE 1년, COMMUNICATION 1개월, STORE/FESTIVAL 이벤트 기간).
    @Query(value = """
            SELECT
                p.pin_id        AS pinId,
                p.pin_type      AS pinType,
                ST_Y(pl.pin_point) AS lat,
                ST_X(pl.pin_point) AS lng,
                pl.detail_address  AS detailAddress,
                l.location         AS region,
                CASE WHEN p.pin_type = 'STORE' THEN ep.discount ELSE NULL END AS discount
            FROM pin_location pl
            INNER JOIN pin      p ON pl.pin_id      = p.pin_id
            INNER JOIN location l ON pl.location_id = l.location_id
            LEFT JOIN communication_pin cp ON cp.pin_id = p.pin_id
            LEFT JOIN event_pin ep ON ep.pin_id = p.pin_id
            WHERE p.created_at >= (NOW() AT TIME ZONE 'Asia/Seoul') - INTERVAL '1 year'
              AND (
                    p.pin_type = 'ISSUE'
                    OR (p.pin_type = 'COMMUNICATION'
                        AND cp.communication_pin_id IS NOT NULL
                        AND COALESCE(cp.updated_at, cp.created_at) >= (NOW() AT TIME ZONE 'Asia/Seoul') - INTERVAL '1 month')
                    OR (p.pin_type IN ('STORE', 'FESTIVAL')
                        AND ep.event_pin_id IS NOT NULL
                        AND (NOW() AT TIME ZONE 'Asia/Seoul') BETWEEN ep.event_start_time AND ep.event_end_time)
                  )
            """, nativeQuery = true)
    List<MapPinView> findAllActivePins();
}
