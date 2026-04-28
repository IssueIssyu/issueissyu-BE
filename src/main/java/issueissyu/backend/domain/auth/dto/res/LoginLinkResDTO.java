package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginLinkResDTO {

    private String uuid;

    @JsonProperty("social_type")
    private String socialType;

    private String nickname;
    private String email;
    private String phone;

    @JsonProperty("event_alarm_active")
    private boolean eventAlarmActive;

    @JsonProperty("like_alarm_active")
    private boolean likeAlarmActive;

    @JsonProperty("hot_alarm_active")
    private boolean hotAlarmActive;

    @JsonProperty("store_alarm_active")
    private boolean storeAlarmActive;

    @JsonProperty("is_available_nickname")
    private boolean isAvailableNickname;

    @JsonProperty("user_phone_verified")
    private boolean userPhoneVerified;
}
