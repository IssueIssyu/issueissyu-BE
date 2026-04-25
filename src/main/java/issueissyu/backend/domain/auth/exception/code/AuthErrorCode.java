package issueissyu.backend.domain.auth.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    NAVER_LOGIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "NAVER_LOGIN_401", "유효하지 않은 값 존재(만료된 인가코드 등)"),
    NAVER_API_FAILED(HttpStatus.BAD_GATEWAY, "NAVER_5021", "네이버 서버 응답에 실패했습니다."),
    TERM_405(HttpStatus.METHOD_NOT_ALLOWED, "TERM_405", "필수 약관(SERVICE, PRIVACY)에 모두 동의해야 합니다."),

    REFRESH_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_401", "토큰 재발급에 실패했습니다."),
    LOGOUT_INVALID(HttpStatus.UNAUTHORIZED, "LOGOUT_401", "유효하지 않은 토큰입니다."),
    SIGNOUT_INVALID(HttpStatus.UNAUTHORIZED, "SIGNOUT_401", "유효하지 않은 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
