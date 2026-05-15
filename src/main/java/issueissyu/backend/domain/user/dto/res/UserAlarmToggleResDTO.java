package issueissyu.backend.domain.user.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAlarmToggleResDTO {

    private final Boolean likeAlarmActive;
    private final Boolean eventAlarmActive;
    private final Boolean hotAlarmActive;
    private final Boolean storeAlarmActive;
}
