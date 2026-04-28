package issueissyu.backend.domain.user.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermResDTO {

    @JsonProperty("event_alarm_active")
    private final boolean eventAlarmActive;

    @JsonProperty("like_alarm_active")
    private final boolean likeAlarmActive;

    @JsonProperty("hot_alarm_active")
    private final boolean hotAlarmActive;

    @JsonProperty("store_alarm_active")
    private final boolean storeAlarmActive;
}
