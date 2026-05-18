package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("POLICY")
public record PolicyCommunityFeedItemResDTO(
        Long communityId,
        String title,
        String content,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO, CommunityDetailItemResDTO {
}