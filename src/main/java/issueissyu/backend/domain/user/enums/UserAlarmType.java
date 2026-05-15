package issueissyu.backend.domain.user.enums;

import issueissyu.backend.domain.user.exception.UserException;
import issueissyu.backend.domain.user.exception.code.UserErrorCode;

public enum UserAlarmType {

    LIKE,
    EVENT,
    HOT,
    STORE;

    public static UserAlarmType fromToken(String raw) {
        if (raw == null || raw.isBlank()) {
            throw UserException.of(UserErrorCode.USER_ALARM_400);
        }
        try {
            return UserAlarmType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw UserException.of(UserErrorCode.USER_ALARM_400);
        }
    }
}
