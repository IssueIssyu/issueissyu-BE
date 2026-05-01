package issueissyu.backend.domain.location.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LocationSuccessCode implements BaseSuccessCode {
    LOCATION_SIGUNGU_MATCH_SUCCESS(HttpStatus.OK, "LOCATION_200_1", "시군구 비교에 성공했습니다."),
    LOCATION_GEOCODE_SUCCESS(HttpStatus.OK, "LOCATION_200_2", "지오코딩에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
