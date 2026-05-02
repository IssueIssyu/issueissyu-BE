package issueissyu.backend.domain.map.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MapSuccessCode implements BaseSuccessCode {

    MAP_200(HttpStatus.OK, "MAP_200", "현재 화면의 전체 핀 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
