package issueissyu.backend.domain.community.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.community.popularity.enabled", havingValue = "true", matchIfMissing = true)
public class CommunityPopularityRankingScheduler {

    private final CommunityPopularityRankingBatchService batchService;

    @Scheduled(cron = "${app.community.popularity.refresh-cron:0 5 4 * * *}", zone = "Asia/Seoul")
    public void refreshDaily() {
        try {
            batchService.refreshAllCommunityPopularity();
        } catch (Exception e) {
            log.error("커뮤니티 인기도 배치 실패", e);
        }
    }
}
