package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDateTime;

@JsonTypeName("STORE")
public record StoreCommunityFeedItemResDTO(
        Long communityId,
        String pinTitle,
        String storeImageUrl,
        String content,
        String discount,
        String pinDetailAddress,
        LocalDateTime eventStartTime,
        LocalDateTime eventEndTime,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO, CommunityDetailItemResDTO {
}
