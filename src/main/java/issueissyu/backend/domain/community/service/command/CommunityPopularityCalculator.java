package issueissyu.backend.domain.community.service.command;

import java.time.LocalDateTime;

public interface CommunityPopularityCalculator {

    // 커뮤니티 게시글의 HOT 정렬용 인기도 점수를 계산한다.
    double calculate(
            int viewCount,
            int likeCount,
            int commentCount,
            int emojiReactionCount,
            LocalDateTime createdAt
    );
}