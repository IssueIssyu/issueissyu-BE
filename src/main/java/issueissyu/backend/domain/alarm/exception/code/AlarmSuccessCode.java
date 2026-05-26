package issueissyu.backend.domain.alarm.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AlarmSuccessCode implements BaseSuccessCode {

    PUSH_TOKEN_200(HttpStatus.OK, "PUSH_TOKEN_200", "push_token이 성공적으로 저장되었습니다."),
    LIKE_ALARM_200(HttpStatus.OK, "LIKE_ALARM_200", "푸시 알림이 성공적으로 전송되었습니다."),
    EVENT_ALARM_200(HttpStatus.OK, "EVENT_ALARM_200", "푸시 알림이 성공적으로 전송되었습니다."),
    STORE_ALARM_200(HttpStatus.OK, "STORE_ALARM_200", "푸시 알림이 성공적으로 전송되었습니다."),
    ALARM_LIST_200(HttpStatus.OK, "ALARM_LIST_200", "알람 목록 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
