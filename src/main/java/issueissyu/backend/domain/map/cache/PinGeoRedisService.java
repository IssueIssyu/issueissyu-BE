package issueissyu.backend.domain.map.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import issueissyu.backend.domain.map.dto.res.MapPinResDTO;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import issueissyu.backend.domain.map.enums.MapPinCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.BoundingBox;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Redis GEO를 활용한 핀 위치 캐싱 서비스.

// 두 트랙 전략입니다.
//  - geo:pins         : 전체 핀 GEO Set (member = pinId 문자열)
//  - geo:pins:{TYPE}  : 타입별 GEO Set (ISSUE / COMMUNICATION / STORE / FESTIVAL)
//  - pin:info:{pinId} : 핀 렌더링 메타데이터 JSON (48시간 TTL)

// GEO Set 자체는 TTL이 없으며, 매일 새벽 스케줄러가 DB 기준으로 전체 재적재합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class PinGeoRedisService {

    static final String GEO_KEY_ALL = "geo:pins";
    static final String GEO_KEY_PREFIX = "geo:pins:";
    static final String PIN_INFO_KEY_PREFIX = "pin:info:";
    private static final Duration PIN_INFO_TTL = Duration.ofHours(48);
    private static final int GEO_SEARCH_LIMIT = 1000;

    // 위도 1도 ≈ 111 km
    private static final double DEGREE_TO_KM = 111.0;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // bulkPopulate 가 완전히 끝난 시점에만 true 가 됩니다.
    // @Async / 스케줄러 스레드에서 쓰고, HTTP 요청 스레드에서 읽으므로 volatile 필수입니다.
    private volatile boolean isReady = false;

    // ─────────────────────────────────────────────────────────────────
    // Write 흐름
    // ─────────────────────────────────────────────────────────────────

    // 핀을 GEO Set 과 pin:info 에 저장합니다.
    // 활성 DB 트랜잭션이 있으면 커밋 완료 후에 Redis 쓰기를 실행합니다.
    // DB 롤백 시 afterCommit 은 호출되지 않으므로 Redis 불일치가 발생하지 않습니다.
    public void addPin(Long pinId, String pinType, double lat, double lng,
                       String detailAddress, String region, String discount) {
        Runnable action = () -> {
            try {
                String member = pinId.toString();
                Point point = new Point(lng, lat); // Redis GEO: (경도, 위도)

                redisTemplate.opsForGeo().add(GEO_KEY_ALL, point, member);
                redisTemplate.opsForGeo().add(GEO_KEY_PREFIX + pinType, point, member);

                MapPinResDTO.PinItemDTO dto =
                        new MapPinResDTO.PinItemDTO(pinId, pinType, lat, lng, detailAddress, region, discount);
                String json = objectMapper.writeValueAsString(dto);
                redisTemplate.opsForValue().set(PIN_INFO_KEY_PREFIX + pinId, json, PIN_INFO_TTL);
            } catch (Exception e) {
                log.warn("Redis GEO 핀 추가 실패 pinId={}: {}", pinId, e.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    // GEO Set 과 pin:info 에서 핀을 삭제합니다.
    // 활성 DB 트랜잭션이 있으면 커밋 완료 후에 Redis 삭제를 실행합니다.
    // DB 롤백 시 afterCommit 은 호출되지 않으므로 핀이 DB·Redis 양쪽에 그대로 유지됩니다.
    public void removePin(Long pinId, String pinType) {
        Runnable action = () -> {
            try {
                String member = pinId.toString();
                redisTemplate.opsForGeo().remove(GEO_KEY_ALL, member);
                redisTemplate.opsForGeo().remove(GEO_KEY_PREFIX + pinType, member);
                redisTemplate.delete(PIN_INFO_KEY_PREFIX + pinId);
            } catch (Exception e) {
                log.warn("Redis GEO 핀 삭제 실패 pinId={}: {}", pinId, e.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }



    // ─────────────────────────────────────────────────────────────────
    // Read 흐름
    // ─────────────────────────────────────────────────────────────────

    // 전체 적재 완료 여부(isReady)와 실제 데이터 존재 여부(ZCARD > 0)를 함께 확인합니다.
    // Redis가 외부에서 초기화되어 isReady=true 이지만 데이터가 없는 경우도 DB 폴백합니다.
    public boolean isGeoSetReady() {
        return isReady && getGeoPinCount() > 0;
    }

    // Redis GEO Set(geo:pins)에 저장된 핀 개수. 조회 실패 시 0.
    public long getGeoPinCount() {
        try {
            Long size = redisTemplate.opsForZSet().size(GEO_KEY_ALL);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.warn("Redis GEO 상태 확인 실패: {}", e.getMessage());
            return 0L;
        }
    }

    // BBox(남서~북동 경위도)로 핀을 검색합니다.

    // 내부적으로 BBox 중심을 구하고 BYBOX GEOSEARCH 를 수행한 뒤,
    // BBox 경계 안에 있는 핀만 정확히 필터링하여 반환합니다.

    // @return 검색 성공 시 Optional<List>, Redis 오류 시 Optional.empty()
    public Optional<List<MapPinResDTO.PinItemDTO>> searchByBBox(
            double swLng, double swLat, double neLng, double neLat, String pinTypeFilter) {
        try {
            double centerLng = (swLng + neLng) / 2.0;
            double centerLat = (swLat + neLat) / 2.0;

            // 도→km 변환 (경도는 위도에 따라 보정, 5 % 버퍼)
            double widthKm = (neLng - swLng) * DEGREE_TO_KM
                    * Math.cos(Math.toRadians(centerLat)) * 1.05;
            double heightKm = (neLat - swLat) * DEGREE_TO_KM * 1.05;

            String geoKey = (pinTypeFilter != null)
                    ? GEO_KEY_PREFIX + pinTypeFilter
                    : GEO_KEY_ALL;

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    redisTemplate.opsForGeo().search(
                            geoKey,
                            GeoReference.fromCoordinate(new Point(centerLng, centerLat)),
                            new BoundingBox(widthKm, heightKm, Metrics.KILOMETERS),
                            GeoSearchCommandArgs.newGeoSearchArgs()
                                    .sortAscending()
                                    .limit(GEO_SEARCH_LIMIT)
                    );

            if (results == null) {
                return Optional.of(Collections.emptyList());
            }

            List<String> pinIdStrings = results.getContent().stream()
                    .map(r -> r.getContent().getName())
                    .toList();

            if (pinIdStrings.isEmpty()) {
                return Optional.of(Collections.emptyList());
            }

            List<String> keys = pinIdStrings.stream()
                    .map(id -> PIN_INFO_KEY_PREFIX + id)
                    .toList();
            List<String> jsonList = redisTemplate.opsForValue().multiGet(keys);

            if (jsonList == null) {
                // MGET 자체가 실패한 경우 DB 폴백
                return Optional.empty();
            }

            List<MapPinResDTO.PinItemDTO> pins = new ArrayList<>();
            for (String json : jsonList) {
                if (json == null) {
                    // GEO Set에는 있지만 pin:info 가 Eviction/TTL 만료된 경우
                    // 일부만 반환하면 핀이 지도에서 누락되므로 DB 폴백으로 전체 데이터 보장
                    log.debug("Redis GEO 캐시 불일치 감지 (pin:info 누락) → DB 폴백");
                    return Optional.empty();
                }
                MapPinResDTO.PinItemDTO dto =
                        objectMapper.readValue(json, MapPinResDTO.PinItemDTO.class);
                // BYBOX 검색 결과에 미세한 오차가 있을 수 있으므로 BBox 정확 필터링
                if (dto.latitude() >= swLat && dto.latitude() <= neLat
                        && dto.longitude() >= swLng && dto.longitude() <= neLng) {
                    pins.add(dto);
                }
            }
            return Optional.of(pins);

        } catch (Exception e) {
            log.warn("Redis GEO BBox 검색 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }



    // ─────────────────────────────────────────────────────────────────
    // Bulk 초기화 / 스케줄러
    // ─────────────────────────────────────────────────────────────────

    // GEO Set 을 초기화한 뒤, DB에서 조회한 활성 핀 목록으로 전체 재적재합니다.
    // 서버 시작 및 매일 새벽 스케줄러에서 호출됩니다.
    // executePipelined 로 모든 명령을 단일 파이프라인에 묶어 네트워크 왕복을 최소화합니다.
    // (개별 addPin: 핀당 3 round-trip → pipeline: 전체 N개를 1회 전송)
    public void bulkPopulate(List<MapPinView> views) {
        isReady = false; // 재적재 시작: 불완전한 캐시를 읽지 않도록 차단
        clearGeoKeys();
        if (views.isEmpty()) {
            isReady = true; // 빈 결과도 완성된 상태이며, getGeoPinCount() == 0 으로 DB 폴백됨
            return;
        }
        try {
            redisTemplate.executePipelined(new org.springframework.data.redis.core.SessionCallback<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public <K, V> Object execute(org.springframework.data.redis.core.RedisOperations<K, V> operations) {
                    // executePipelined 는 RedisTemplate 자신을 operations 로 전달하므로
                    // 인터페이스 타입으로 캐스트하여 파이프라인 내 명령을 큐잉합니다.
                    org.springframework.data.redis.core.RedisOperations<String, String> ops =
                            (org.springframework.data.redis.core.RedisOperations<String, String>) operations;

                    for (MapPinView view : views) {
                        if (view.getPinId() == null || view.getLat() == null || view.getLng() == null) {
                            continue;
                        }
                        try {
                            Long pinId = view.getPinId();
                            String pinType = view.getPinType();
                            double lat = view.getLat();
                            double lng = view.getLng();
                            String member = pinId.toString();
                            Point point = new Point(lng, lat);

                            ops.opsForGeo().add(GEO_KEY_ALL, point, member);
                            ops.opsForGeo().add(GEO_KEY_PREFIX + pinType, point, member);

                            MapPinResDTO.PinItemDTO dto = new MapPinResDTO.PinItemDTO(
                                    pinId, pinType, lat, lng,
                                    view.getDetailAddress(), view.getRegion(), view.getDiscount()
                            );
                            String json = objectMapper.writeValueAsString(dto);
                            ops.opsForValue().set(PIN_INFO_KEY_PREFIX + pinId, json, PIN_INFO_TTL);
                        } catch (Exception e) {
                            log.warn("Redis GEO bulk populate 단일 핀 직렬화 실패 pinId={}: {}",
                                    view.getPinId(), e.getMessage());
                        }
                    }
                    return null;
                }
            });
            isReady = true; // 파이프라인이 Redis에서 완전히 처리된 후에만 준비 완료 표시
        } catch (Exception e) {
            log.error("Redis GEO bulk populate 파이프라인 실패: {}", e.getMessage(), e);
            // isReady = false 유지 → DB 폴백으로 동작
        }
    }

    // 모든 geo:pins, geo:pins:{TYPE} 키를 삭제합니다.
    // pin:info 키는 48h TTL 으로 자연 만료되므로 별도 삭제하지 않습니다.
    // (재적재 시 addPin 이 TTL 을 갱신합니다.)
    //
    // KEYS 명령어(O(N), 전체 DB 블로킹) 대신 MapPinCategory 열거값으로
    // 삭제 대상 키를 직접 구성하여 단일 DEL 명령으로 처리합니다.
    private void clearGeoKeys() {
        try {
            List<String> keysToDelete = new ArrayList<>();
            keysToDelete.add(GEO_KEY_ALL);
            for (MapPinCategory category : MapPinCategory.values()) {
                keysToDelete.add(GEO_KEY_PREFIX + category.getPinType());
            }
            redisTemplate.delete(keysToDelete);
        } catch (Exception e) {
            log.warn("Redis GEO 키 전체 삭제 실패: {}", e.getMessage());
        }
    }
}
