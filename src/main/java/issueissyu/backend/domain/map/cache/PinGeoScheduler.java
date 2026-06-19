package issueissyu.backend.domain.map.cache;

import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.dto.res.MapPinView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

// Redis GEO 캐시 정합성 유지를 위한 야간 배치 스케줄러.

// 다중 서버 환경에서 모든 인스턴스가 동시에 실행되는 문제를 방지하기 위해
// Redis SET NX PX 기반 분산 락을 사용합니다.
// 락을 획득한 인스턴스만 재구성을 수행하고, 나머지는 즉시 건너뜁니다.
// 락 TTL 이 만료되면 (프로세스 비정상 종료 등) 다음 실행 시 자동으로 획득 가능합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class PinGeoScheduler {

    private static final String REBUILD_LOCK_KEY = "lock:geo:rebuild";
    // 재구성 예상 소요 시간보다 충분히 길게 설정 (비정상 종료 시 자동 만료용)
    private static final Duration REBUILD_LOCK_TTL = Duration.ofMinutes(10);

    private final PinLocationRepository pinLocationRepository;
    private final PinGeoRedisService pinGeoRedisService;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 25 13 * * *", zone = "Asia/Seoul")
    public void rebuildGeoCache() {
        // SET lock:geo:rebuild 1 NX PX 600000 — 원자적 명령으로 레이스 컨디션 없음
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(REBUILD_LOCK_KEY, "1", REBUILD_LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            log.info("[Redis GEO] 다른 인스턴스가 야간 캐시 재구성 중이므로 건너뜁니다.");
            return;
        }
        try {
            log.info("[Redis GEO] 야간 캐시 재구성 시작 ...");
            List<MapPinView> views = pinLocationRepository.findAllActivePins();
            pinGeoRedisService.bulkPopulate(views);
            log.info("[Redis GEO] 야간 캐시 재구성 완료: {}개 핀", views.size());
        } catch (Exception e) {
            log.error("[Redis GEO] 야간 캐시 재구성 실패: {}", e.getMessage(), e);
        } finally {
            // 정상·비정상 종료 모두 락 즉시 해제 (다음 예약 실행까지 기다릴 필요 없음)
            redisTemplate.delete(REBUILD_LOCK_KEY);
        }
    }
}
