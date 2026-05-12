package issueissyu.backend.domain.user.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermResDTO {

    @JsonProperty("eventAlarmActive")
    private final boolean eventAlarmActive;

    @JsonProperty("likeAlarmActive")
    private final boolean likeAlarmActive;

    @JsonProperty("hotAlarmActive")
    private final boolean hotAlarmActive;

    @JsonProperty("storeAlarmActive")
    private final boolean storeAlarmActive;
}
