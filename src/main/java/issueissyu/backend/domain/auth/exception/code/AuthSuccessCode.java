package issueissyu.backend.domain.auth.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    REFRESH_200(HttpStatus.OK, "REFRESH_200", "토큰 재발급에 성공했습니다.");

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
