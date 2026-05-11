package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ISSUE")
public record IssueCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String title,
        String pinImageUrl,
        String pinUserNickname,
        String pinUserProfile,
        String pinDetailAddress,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO {
}
