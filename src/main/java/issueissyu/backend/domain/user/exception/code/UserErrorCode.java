package issueissyu.backend.domain.user.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_PIN_400_1(HttpStatus.BAD_REQUEST, "USER_PIN_400_1", "조회 불가능한 사이즈 입니다."),
    USER_PIN_400_2(HttpStatus.BAD_REQUEST, "USER_PIN_400_2", "조회 불가능한 cursor 입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
