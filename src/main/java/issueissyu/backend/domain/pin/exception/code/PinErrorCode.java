package issueissyu.backend.domain.pin.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PinErrorCode implements BaseErrorCode {

    // 핀
    PIN_NOT_FOUND(HttpStatus.NOT_FOUND, "PIN_NOT_FOUND_404", "존재하지 않는 핀입니다."),

    // 핀 이모지 반응
    EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND, "EMOJI_NOT_FOUND_404_1", "존재하지 않는 이모지입니다."),
    EMOJI_NOT_OWNED(HttpStatus.FORBIDDEN, "EMOJI_NOT_OWNED_403_1", "구매하지 않은 이모지입니다."),
    MY_EMOJI_NOT_FOUND(HttpStatus.NOT_FOUND, "MY_EMOJI_NOT_FOUND_404_2", "취소할 내 반응이 없습니다."),

    // 핀 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND_404_3", "존재하지 않는 댓글입니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_FORBIDDEN_403_2", "댓글 수정/삭제 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
