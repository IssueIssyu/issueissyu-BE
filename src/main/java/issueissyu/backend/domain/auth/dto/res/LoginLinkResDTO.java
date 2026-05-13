package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginLinkResDTO {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    private String uuid;

    @JsonProperty("socialType")
    private String socialType;

    private String nickname;
    private String email;
    private String phone;

    @JsonProperty("eventAlarmActive")
    private boolean eventAlarmActive;

    @JsonProperty("likeAlarmActive")
    private boolean likeAlarmActive;

    @JsonProperty("hotAlarmActive")
    private boolean hotAlarmActive;

    @JsonProperty("storeAlarmActive")
    private boolean storeAlarmActive;

    @Getter(AccessLevel.NONE)
    @JsonProperty("isAvailableNickname")
    private boolean isAvailableNickname;

    @JsonProperty("isAvailableNickname")
    public boolean isAvailableNickname() {
        return isAvailableNickname;
    }

    @JsonProperty("userPhoneVerified")
    private boolean userPhoneVerified;
}
