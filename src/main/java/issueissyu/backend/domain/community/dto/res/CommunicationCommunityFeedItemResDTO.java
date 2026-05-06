package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("COMMUNICATION")
public record CommunicationCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String title,
        String thumbnailUrl,
        String authorNickname,
        String authorProfileUrl,
        String address,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO {
}
