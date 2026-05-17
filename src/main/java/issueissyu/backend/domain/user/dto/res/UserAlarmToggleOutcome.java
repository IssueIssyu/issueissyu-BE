package issueissyu.backend.domain.user.dto.res;

import issueissyu.backend.domain.user.exception.code.UserSuccessCode;

public record UserAlarmToggleOutcome(UserSuccessCode successCode, UserAlarmToggleResDTO result) {
}
