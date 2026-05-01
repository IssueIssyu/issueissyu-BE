package issueissyu.backend.domain.auth.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    NAVER_LOGIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "NAVER_LOGIN_401", "유효하지 않은 값이 존재합니다(만료된 인가코드 등)."),
    NAVER_API_FAILED(HttpStatus.BAD_GATEWAY, "NAVER_5021", "네이버 서버 응답에 실패했습니다."),
    KAKAO_LOGIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "KAKAO_LOGIN_401", "유효하지 않은 값이 존재합니다."),
    NICKNAME_400(HttpStatus.BAD_REQUEST, "NICKNAME_400", "닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다."),
    NICKNAME_409(HttpStatus.CONFLICT, "NICKNAME_409", "이미 사용 중인 닉네임입니다."),
    TERM_400(HttpStatus.BAD_REQUEST, "TERM_400", "필수 약관(SERVICE, PRIVACY)에 모두 동의해야 합니다."),

    // 전화번호 인증
    PHONE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PHONE_SEND_500", "SMS 전송에 실패했습니다."),
    PHONE_CODE_INVALID(HttpStatus.BAD_REQUEST, "PHONE_CODE_400", "인증번호가 일치하지 않거나 만료되었습니다."),
    PHONE_400(HttpStatus.BAD_REQUEST, "PHONE_400", "중복된 전화번호입니다."),

    // 로그인 연동
    LOGIN_LINK_400(HttpStatus.BAD_REQUEST, "LOGIN_LINK_400", "로그인 연동 실패"),
    ONBOAREDING_400(HttpStatus.BAD_REQUEST, "ONBOAREDING_400", "온보딩 실패"),

    REFRESH_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_401", "토큰 재발급에 실패했습니다."),
    LOGOUT_INVALID(HttpStatus.UNAUTHORIZED, "LOGOUT_401", "유효하지 않은 토큰입니다."),
    SIGNOUT_INVALID(HttpStatus.UNAUTHORIZED, "SIGNOUT_401", "유효하지 않은 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .httpStatus(this.httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }
}
