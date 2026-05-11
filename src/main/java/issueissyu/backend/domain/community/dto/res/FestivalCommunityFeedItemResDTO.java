package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDateTime;

@JsonTypeName("FESTIVAL")
public record FestivalCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String title,
        String thumbnailUrl,
        String address,
        int viewCount,
        long likeCount,
        LocalDateTime eventStartTime,
        LocalDateTime eventEndTime
) implements CommunityFeedItemResDTO {
}
