package issueissyu.backend.domain.map.cache;

import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

// 서버 시작 시 DB의 활성 핀을 Redis GEO 캐시에 전체 적재합니다.
// {@link ApplicationReadyEvent} 이후 비동기로 실행되어 서버 기동 지연이 없습니다.
// 초기화 도중 핀 조회 API 호출이 오면 isGeoSetReady() == false 이므로 DB에서 응답합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class PinGeoRedisInitializer {

    private final PinLocationRepository pinLocationRepository;
    private final PinGeoRedisService pinGeoRedisService;

    @Async
    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            log.info("[Redis GEO] 캐시 초기화 시작 ...");
            List<MapPinView> views = pinLocationRepository.findAllActivePins();
            pinGeoRedisService.bulkPopulate(views);
            log.info("[Redis GEO] 캐시 초기화 완료: {}개 핀 적재", views.size());
        } catch (Exception e) {
            log.warn("[Redis GEO] 캐시 초기화 실패 (서버는 정상 기동, DB 폴백 사용): {}", e.getMessage());
        }
    }
}
