package issueissyu.backend.domain.auth.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    // 네이버 앱 로그인 성공 코드
    NAVER_LOGIN_200_1(HttpStatus.OK, "NAVER_LOGIN_200_1", "첫 로그인에 성공했습니다."),
    NAVER_LOGIN_200_2(HttpStatus.OK, "NAVER_LOGIN_200_2", "로그인에 성공했습니다."),
    NICKNAME_200(HttpStatus.OK, "NICKNAME_200", "사용 가능한 닉네임입니다."),

    REFRESH_200(HttpStatus.OK, "REFRESH_200", "토큰 재발급에 성공했습니다."),
    LOGOUT_200(HttpStatus.OK, "LOGOUT_200", "로그아웃 되었습니다."),
    SIGNOUT_200(HttpStatus.OK, "SIGNOUT_200", "회원탈퇴 되었습니다.");

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
