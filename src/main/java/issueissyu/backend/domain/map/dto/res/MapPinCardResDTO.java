package issueissyu.backend.domain.map.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MapPinCardResDTO {

    private Long pinId;
    private String pinType;
    private String pinTitle;
    private String pinContent;
    private String issuePinState;
    private String pinDetailAddress;
    private Long likeCount;

    @JsonProperty("isLike")
    private boolean likedByMe;

    @JsonProperty("isMine")
    private boolean mine;

    private String pinUserId;
    private String pinUserProfile;
    private String pinUserNickname;
    private String pinImageUrl;
    private String discount;
    private String storeImageUrl;
}
