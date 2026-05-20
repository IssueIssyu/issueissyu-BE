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
        Pin pin = getRequiredPin(community);

        int viewCount = pin.getViewCount();
        int likeCount = pin.getLikeCount();
        int commentCount = safeToInt(commentRepository.countByPin_PinId(pin.getPinId()));
        int emojiReactionCount = safeToInt(pinEmojiRepository.countActiveByPinId(pin.getPinId()));

        double popularity = communityPopularityCalculator.calculate(
                viewCount,
                likeCount,
                commentCount,
                emojiReactionCount,
                community.getCreatedAt()
        );

        community.updatePopularity(popularity);
    }

    private Pin getRequiredPin(Community community) {
        if (community.getPin() == null) {
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
}