package issueissyu.backend.domain.pin.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PinErrorCode implements BaseErrorCode {
    PIN_NOT_FOUND(HttpStatus.NOT_FOUND, "PIN_404_1", "존재하지 않는 핀입니다."),
    EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND, "PIN_404_2", "존재하지 않는 이모지입니다."),
    EMOJI_NOT_OWNED(HttpStatus.FORBIDDEN, "PIN_403_1", "구매하지 않은 이모지입니다."),
    MY_EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND, "PIN_404_3", "취소할 내 반응이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
