package issueissyu.backend.domain.community.service.command;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CommunityPopularityCalculatorImpl implements CommunityPopularityCalculator {

    private static final double VIEW_WEIGHT = 0.01;
    private static final double LIKE_WEIGHT = 0.5;
    private static final double COMMENT_WEIGHT = 0.8;
    private static final double EMOJI_REACTION_WEIGHT = 0.3;

    // 시간이 지날수록 점수를 줄이는 정도
    // 값이 클수록 오래된 글이 더 빠르게 내려간다.
    private static final double TIME_DECAY_WEIGHT = 0.05;

    @Override
    public double calculate(
            int viewCount,
            int likeCount,
            int commentCount,
            int emojiReactionCount,
            LocalDateTime createdAt
    ) {
        double rawScore = calculateRawScore(
                viewCount,
                likeCount,
                commentCount,
                emojiReactionCount
        );

        double timeWeight = calculateTimeWeight(createdAt);

        return rawScore * timeWeight;
    }

    private double calculateRawScore(
            int viewCount,
            int likeCount,
            int commentCount,
            int emojiReactionCount
    ) {
        return (viewCount * VIEW_WEIGHT)
                + (likeCount * LIKE_WEIGHT)
                + (commentCount * COMMENT_WEIGHT)
                + (emojiReactionCount * EMOJI_REACTION_WEIGHT);
    }

    private double calculateTimeWeight(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 1.0;
        }

        long hours = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());

        if (hours < 0) {
            hours = 0;
        }

        return 1.0 / (1.0 + (hours * TIME_DECAY_WEIGHT));
    }
}