package issueissyu.backend.domain.community.service.command;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinEmojiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityPopularityServiceImpl implements CommunityPopularityService {

    private final CommunityPopularityCalculator communityPopularityCalculator;
    private final CommentRepository commentRepository;
    private final PinEmojiRepository pinEmojiRepository;

    @Override
    public void updatePopularity(Community community) {
        PopularitySource source = resolvePopularitySource(community);

        double popularity = communityPopularityCalculator.calculate(
                source.viewCount(),
                source.likeCount(),
                source.commentCount(),
                source.emojiReactionCount(),
                community.getCreatedAt()
        );

        community.updatePopularity(popularity);
    }

    private PopularitySource resolvePopularitySource(Community community) {

        // 핀 기반 게시물
        if (community.requiresPin()) {
            return resolvePinBasedPopularitySource(community);
        }

        // 커뮤니티 기반 게시물
        return resolveCommunityBasedPopularitySource(community);
    }

    // 핀 기반 게시물 인기도 처리
    private PopularitySource resolvePinBasedPopularitySource(Community community) {
        Pin pin = getRequiredPin(community);

        int viewCount = pin.getViewCount();
        int likeCount = pin.getLikeCount();
        int commentCount = safeToInt(commentRepository.countByPin_PinId(pin.getPinId()));
        int emojiReactionCount = safeToInt(pinEmojiRepository.countActiveByPinId(pin.getPinId()));

        return new PopularitySource(
                viewCount,
                likeCount,
                commentCount,
                emojiReactionCount
        );
    }

    // 커뮤니티 기반 게시물 인기도 처리
    private PopularitySource resolveCommunityBasedPopularitySource(Community community) {
        int viewCount = community.getViewCount();
        int likeCount = community.getLikeCount();

        // 현재 구조에서는 POLICY / CONTEST / CARDNEWS 댓글과 이모지 반응이 Pin 기준이 아님.
        // 추후 CommunityCommentRepository, CommunityEmojiRepository 등이 생기면 여기에서 교체하면 된다.
        int commentCount = 0;
        int emojiReactionCount = 0;

        return new PopularitySource(
                viewCount,
                likeCount,
                commentCount,
                emojiReactionCount
        );
    }

    private Pin getRequiredPin(Community community) {
        if (!community.hasPin()) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_404_1);
        }

        return community.getPin();
    }

    private int safeToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }

    private record PopularitySource(
            int viewCount,
            int likeCount,
            int commentCount,
            int emojiReactionCount
    ) {
    }
}
