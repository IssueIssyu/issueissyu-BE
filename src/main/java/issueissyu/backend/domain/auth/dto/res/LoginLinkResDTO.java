package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginLinkResDTO {

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
    private boolean nicknameAvailability;

    @JsonProperty("isAvailableNickname")
    public boolean isAvailableNickname() {
        return nicknameAvailability;
    }

    @JsonProperty("userPhoneVerified")
    private boolean userPhoneVerified;
}
