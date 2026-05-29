package issueissyu.backend.domain.user.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserSuccessCode implements BaseSuccessCode {

    PATCHNOTE_200(HttpStatus.OK, "PATCHNOTE_200", "내 핀 조회에 성공했습니다."),
    USER_NICKNAME_200(HttpStatus.OK, "USER_NICKNAME_200", "닉네임 변경에 성공했습니다."),
    USER_ALARM_200_1(HttpStatus.OK, "USER_ALARM_200_1", "핀 좋아요 알람 비/활성화에 성공했습니다."),
    USER_ALARM_200_2(HttpStatus.OK, "USER_ALARM_200_2", "이벤트 알람 비/활성화에 성공했습니다."),
    USER_ALARM_200_3(HttpStatus.OK, "USER_ALARM_200_3", "인기 게시글 알람 비/활성화에 성공했습니다."),
    USER_ALARM_200_4(HttpStatus.OK, "USER_ALARM_200_4", "가게 홍보 알람 비/활성화에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
