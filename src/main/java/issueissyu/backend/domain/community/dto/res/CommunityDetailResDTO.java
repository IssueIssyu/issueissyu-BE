package issueissyu.backend.domain.community.dto.res;

import java.time.LocalDateTime;

public record CommunityDetailResDTO(
        CommunityFeedItemResDTO item,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
