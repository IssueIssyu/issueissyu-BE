package issueissyu.backend.domain.alarm.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AlarmErrorCode implements BaseErrorCode {

    ALARM_TOKEN_404(HttpStatus.NOT_FOUND, "ALARM_TOKEN_404", "유효하지 않은 토큰을 입력하였습니다."),

    LIKE_ALARM_404(HttpStatus.NOT_FOUND, "LIKE_ALARM_404", "존재하지 않는 좋아요 알람입니다."),
    LIKE_ALARM_403(HttpStatus.FORBIDDEN, "LIKE_ALARM_403", "해당 사용자는 푸시 알림 수신을 제한해두었습니다."),
    LIKE_ALARM_400(HttpStatus.BAD_REQUEST, "LIKE_ALARM_400", "푸시 알림 전송에 실패했습니다."),

    EVENT_ALARM_404(HttpStatus.NOT_FOUND, "EVENT_ALARM_404", "존재하지 않는 이벤트 알람입니다."),
    EVENT_ALARM_403(HttpStatus.FORBIDDEN, "EVENT_ALARM_403", "해당 사용자는 푸시 알림 수신을 제한해두었습니다."),
    EVENT_ALARM_400(HttpStatus.BAD_REQUEST, "EVENT_ALARM_400", "푸시 알림 전송에 실패했습니다."),

    STORE_ALARM_404(HttpStatus.NOT_FOUND, "STORE_ALARM_404", "존재하지 않는 가게 알람입니다."),
    STORE_ALARM_403(HttpStatus.FORBIDDEN, "STORE_ALARM_403", "해당 사용자는 푸시 알림 수신을 제한해두었습니다."),
    STORE_ALARM_400(HttpStatus.BAD_REQUEST, "STORE_ALARM_400", "푸시 알림 전송에 실패했습니다."),

    ALARM_LIST_400_1(HttpStatus.BAD_REQUEST, "ALARM_LIST_400_1", "조회 불가능한 사이즈 입니다."),
    ALARM_LIST_400_2(HttpStatus.BAD_REQUEST, "ALARM_LIST_400_2", "조회 불가능한 cursor 입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
