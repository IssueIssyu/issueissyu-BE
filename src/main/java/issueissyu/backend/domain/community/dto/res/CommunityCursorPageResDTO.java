package issueissyu.backend.domain.community.dto.res;

import java.util.List;

public record CommunityCursorPageResDTO(
        String region,
        List<CommunityFeedItemResDTO> content,
        String nextCursor,
        boolean hasNext
) {
}
