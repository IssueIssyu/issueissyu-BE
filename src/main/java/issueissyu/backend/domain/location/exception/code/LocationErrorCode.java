package issueissyu.backend.domain.location.exception.code;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LocationErrorCode implements BaseErrorCode {
    LOCATION_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "LOCATION_4001", "잘못된 위치 요청입니다."),
    LOCATION_API_KEY_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "LOCATION_5001", "지도 API 키가 설정되지 않았습니다."),
    LOCATION_GEOCODE_API_FAILED(HttpStatus.BAD_GATEWAY, "LOCATION_5021", "지오코딩 API 호출에 실패했습니다."),
    LOCATION_REVERSE_GEOCODE_API_FAILED(HttpStatus.BAD_GATEWAY, "LOCATION_5022", "리버스 지오코딩 API 호출에 실패했습니다."),
    LOCATION_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "LOCATION_5023", "지도 API 응답이 비어 있습니다."),
    LOCATION_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION_4041", "주소 결과를 찾을 수 없습니다."),
    LOCATION_SIGUNGU_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION_4042", "시군구 결과를 찾을 수 없습니다.");

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
