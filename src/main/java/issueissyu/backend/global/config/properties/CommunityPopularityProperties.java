package issueissyu.backend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 커뮤니티 인기도 갱신 배치 설정 (조회·댓글·좋아요 가중치, 선택적 외부 차트 순위 페널티).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.community.popularity")
public class CommunityPopularityProperties {

    private boolean enabled = true;

    /** 매일 인기도 재계산 시각 (cron 6자리, 초 분 시 …) */
    private String refreshCron = "0 5 4 * * *";

    private double weightView = 0.01;

    private double weightComment = 0.25;

    private double weightLike = 0.25;

    /**
     * 외부 인기 차트(예: PlayDB 순위) 반영 시 게시글마다 빼 줄 값. 미연동 시 0.
     */
    private double chartRankSubtract = 0.0;
}
