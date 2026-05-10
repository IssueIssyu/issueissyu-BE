package issueissyu.backend.domain.location.exception.code;

import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LocationSuccessCode implements BaseSuccessCode {
    LOCATION_SIGUNGU_MATCH_SUCCESS(HttpStatus.OK, "LOCATION_200_1", "시군구 비교에 성공했습니다."),
    LOCATION_GEOCODE_SUCCESS(HttpStatus.OK, "LOCATION_200_2", "지오코딩에 성공했습니다."),
    LOCATION_USER_GET_SUCCESS(HttpStatus.OK, "LOCATION_200_3", "사용자 위치 조회에 성공했습니다."),
    LOCATION_USER_CERT_SUCCESS(HttpStatus.OK, "LOCATION_200_4", "사용자 위치 인증에 성공했습니다."),
    LOCATION_PIN_CREATION_CHECK_SUCCESS(HttpStatus.OK, "LOCATION_200_5", "핀 생성 가능 여부 확인에 성공했습니다."),
    LOCATION_ROAD_ADDRESS_SUCCESS(HttpStatus.OK, "LOCATION_200_6", "도로명 주소 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
