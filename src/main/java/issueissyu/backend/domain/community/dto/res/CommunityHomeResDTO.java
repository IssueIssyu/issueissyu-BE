package issueissyu.backend.domain.community.dto.res;

import java.util.List;

public record CommunityHomeResDTO(
        Long locationId,
        List<CommunityFeedItemResDTO> storePromotions,
        List<CommunityFeedItemResDTO> hotPreviews,
        CommunityCursorPageResDTO recentNews
) {
}
