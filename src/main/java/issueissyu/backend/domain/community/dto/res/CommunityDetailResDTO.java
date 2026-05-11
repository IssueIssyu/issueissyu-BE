package issueissyu.backend.domain.community.dto.res;

import java.time.LocalDateTime;
import java.util.List;

public record CommunityDetailResDTO(
        CommunityDetailItemResDTO item,
        String content,
        List<String> pinImageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isReported,
        Boolean isPetitioned,
        Boolean isProblemSolver
) {
}
