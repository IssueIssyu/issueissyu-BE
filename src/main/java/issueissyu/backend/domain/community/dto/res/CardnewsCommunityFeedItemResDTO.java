package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CARDNEWS")
public record CardnewsCommunityFeedItemResDTO(
        Long communityId,
        String title,
        String content,
        String cardnewsImageUrl,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO, CommunityDetailItemResDTO {
}