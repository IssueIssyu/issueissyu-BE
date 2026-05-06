package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ISSUE")
public record IssueCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String title,
        String thumbnailUrl,
        String authorNickname,
        String authorProfileUrl,
        String address,
        int viewCount,
        long likeCount,
        String issuePinState
) implements CommunityFeedItemResDTO {
}
