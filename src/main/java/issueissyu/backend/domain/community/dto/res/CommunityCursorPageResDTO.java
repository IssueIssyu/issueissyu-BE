package issueissyu.backend.domain.community.dto.res;

public record CommunityCursorPageResDTO(
        java.util.List<CommunityFeedItemResDTO> content,
        String nextCursor,
        boolean hasNext
) {
}
