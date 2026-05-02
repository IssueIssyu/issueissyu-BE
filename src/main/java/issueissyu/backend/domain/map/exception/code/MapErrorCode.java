package issueissyu.backend.domain.map.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MapErrorCode implements BaseErrorCode {

    MAP_400_1(HttpStatus.BAD_REQUEST, "MAP_400_1", "요청 쿼리 파라미터 형식이 맞지 않습니다."),
    MAP_400_2(HttpStatus.BAD_REQUEST, "MAP_400_2", "전체 핀 조회에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
