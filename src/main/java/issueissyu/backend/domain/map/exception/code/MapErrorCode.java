package issueissyu.backend.domain.map.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MapErrorCode implements BaseErrorCode {

    MAP_400_1(HttpStatus.BAD_REQUEST, "MAP_400_1", "잘못된 좌표 입니다."),
    MAP_400_2(HttpStatus.BAD_REQUEST, "MAP_400_2", "존재하지 않는 카테고리 입니다."),
    MAP_400_3(HttpStatus.BAD_REQUEST, "MAP_400_3", "핀 조회에 실패했습니다."),
    MAP_CARD_404(HttpStatus.NOT_FOUND, "MAP_CARD_404", "존재하지 않는 핀 입니다."),
    MAP_CARD_500(HttpStatus.INTERNAL_SERVER_ERROR, "MAP_CARD_500",
            "핀 카드 조회 중 서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
