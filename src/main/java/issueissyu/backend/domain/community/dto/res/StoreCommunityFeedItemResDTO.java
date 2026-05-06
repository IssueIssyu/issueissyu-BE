package issueissyu.backend.domain.community.dto.res;

import com.fasterxml.jackson.annotation.JsonTypeName;

//TODO : 프론트에게 문의 후 확정
@JsonTypeName("STORE")
public record StoreCommunityFeedItemResDTO(
        Long communityId,
        Long pinId,
        String storeName,
        String thumbnailUrl,
        // 관리자용 계정
        String authorNickname,
        String authorProfileUrl,
        String discount, // 할인 정보
        String address,
        int viewCount,
        long likeCount
) implements CommunityFeedItemResDTO {
}
