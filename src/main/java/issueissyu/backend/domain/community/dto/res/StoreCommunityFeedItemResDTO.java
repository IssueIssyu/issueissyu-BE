package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("STORE")
public record StoreCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String storeName,
        String thumbnailUrl,
        String discount,
        String address,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO {
}
