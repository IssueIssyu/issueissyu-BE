package issueissyu.backend.domain.pin.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PinSuccessCode implements BaseSuccessCode {
    GET_PIN_EMOJIS_SUCCESS(HttpStatus.OK, "PIN_200_1", "핀 반응 목록 조회에 성공했습니다."),
    GET_EMOJI_CANDIDATES_SUCCESS(HttpStatus.OK, "PIN_200_2", "반응 후보 목록 조회에 성공했습니다."),
    APPLY_EMOJI_SUCCESS(HttpStatus.OK, "PIN_200_3", "핀 반응 등록에 성공했습니다."),
    DELETE_MY_EMOJI_SUCCESS(HttpStatus.OK, "PIN_200_4", "핀 반응 취소에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
