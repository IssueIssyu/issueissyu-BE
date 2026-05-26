package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import issueissyu.backend.domain.community.enums.CommunityType;

import java.time.LocalDateTime;
import java.util.List;

public record CommunityDetailResDTO(
        CommunityType kind,

        Long communityId,
        Long pinId,

        String title,
        String content,

        List<String> imageUrls,

        String writerNickname,
        String writerProfileUrl,

        String detailAddress,

        int viewCount,
        long likeCount,
        @JsonProperty("isLike") boolean isLike,

        String discount,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime eventStartTime,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime eventEndTime,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        Boolean isReported,
        Boolean isPetitioned,
        Boolean isProblemSolver,

        String issuePinState,
        Integer petitionCount,

        boolean isMine
) {
}