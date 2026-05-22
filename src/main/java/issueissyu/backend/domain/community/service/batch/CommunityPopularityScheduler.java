package issueissyu.backend.domain.community.service.batch;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.community.service.command.CommunityPopularityService;
import issueissyu.backend.global.config.properties.CommunityPopularityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityPopularityScheduler {

    private static final int POPULARITY_RECALC_DAYS = 7;

    private final CommunityRepository communityRepository;
    private final CommunityPopularityService communityPopularityService;
    private final CommunityPopularityProperties communityPopularityProperties;

    // 매일 새벽 4시에 최근 7일 게시글의 popularity를 재계산한다.
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void recalculatePopularity() {
        if (!communityPopularityProperties.isEnabled()) {
            log.debug("Community popularity 배치가 비활성화되어 있습니다. (app.community.popularity.enabled=false)");
            return;
        }

        LocalDateTime since = LocalDateTime.now().minusDays(POPULARITY_RECALC_DAYS);

        List<Community> targets = communityRepository.findPopularityUpdateTargets(since);

        for (Community community : targets) {
            communityPopularityService.updatePopularity(community);
        }

        log.info("Community popularity 재계산 완료. 대상 게시글 수: {}", targets.size());
    }
}