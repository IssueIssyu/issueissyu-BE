package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import issueissyu.backend.domain.community.enums.CommunityType;

import java.time.LocalDateTime;

public record CommunityFeedItemResDTO(
        CommunityType kind,

        Long communityId,
        Long pinId,

        String title,
        String content,

        String thumbnailUrl,

        String writerNickname,
        String writerProfileUrl,

        String detailAddress,

        int viewCount,
        long likeCount,

        String discount,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime eventStartTime,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime eventEndTime
) {
}
