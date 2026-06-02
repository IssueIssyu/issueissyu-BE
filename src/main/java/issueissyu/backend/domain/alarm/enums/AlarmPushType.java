package issueissyu.backend.domain.alarm.enums;

import issueissyu.backend.domain.user.enums.UserAlarmType;

// FCM data payload {@code type} 값. 앱은 {@code PIN_LIKED} 등 enum name 문자열을 사용한다.
public enum AlarmPushType {

    PIN_LIKED,
    PIN_EVENT,
    PIN_POPULAR,
    PIN_STORE_AD;

    public String fcmType() {
        return name();
    }

    public static AlarmPushType fromUserAlarmType(UserAlarmType alarmType) {
        return switch (alarmType) {
            case LIKE -> PIN_LIKED;
            case EVENT -> PIN_EVENT;
            case HOT -> PIN_POPULAR;
            case STORE -> PIN_STORE_AD;
        };
    }
}
