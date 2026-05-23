package issueissyu.backend.domain.community.dto.res;

import java.util.List;

public record CommunityCursorPageResDTO(
        Long locationId,
        List<CommunityFeedItemResDTO> content,
        String nextCursor,
        boolean hasNext
) {
}
