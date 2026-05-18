package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDateTime;

@JsonTypeName("FESTIVAL")
public record FestivalCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String pinTitle,
        String festivalImageUrl,
        String content,
        String discount,
        String pinDetailAddress,
        LocalDateTime eventStartTime,
        LocalDateTime eventEndTime,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO, CommunityDetailItemResDTO {
}