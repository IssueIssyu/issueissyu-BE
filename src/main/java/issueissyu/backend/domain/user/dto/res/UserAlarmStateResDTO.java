package issueissyu.backend.domain.user.dto.res;

public record UserAlarmStateResDTO(
        boolean likeAlarmActive,
        boolean eventAlarmActive,
        boolean hotAlarmActive,
        boolean storeAlarmActive) {}
