package issueissyu.backend.domain.map.cache;

import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// Redis GEO 캐시 정합성 유지를 위한 야간 배치 스케줄러.

// 동작 조건 :
//  매일 새벽 1:30 에 DB 기준 활성 핀 목록으로 캐시 전체 재구성
//  만료 조건: ISSUE 1년, COMMUNICATION 1개월 미반응, STORE/FESTIVAL 이벤트 종료
//  재적재 후 pin:info TTL 이 48시간으로 갱신되어 메모리 낭비를 방지
@Slf4j
@Component
@RequiredArgsConstructor
public class PinGeoScheduler {

    private final PinLocationRepository pinLocationRepository;
    private final PinGeoRedisService pinGeoRedisService;

    @Scheduled(cron = "0 30 1 * * *", zone = "Asia/Seoul")
    public void rebuildGeoCache() {
        try {
            log.info("[Redis GEO] 야간 캐시 재구성 시작 ...");
            List<MapPinView> views = pinLocationRepository.findAllActivePins();
            pinGeoRedisService.bulkPopulate(views);
            log.info("[Redis GEO] 야간 캐시 재구성 완료: {}개 핀", views.size());
        } catch (Exception e) {
            log.error("[Redis GEO] 야간 캐시 재구성 실패: {}", e.getMessage(), e);
        }
    }
}
