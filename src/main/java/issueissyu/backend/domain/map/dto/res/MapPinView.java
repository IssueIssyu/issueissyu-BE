package issueissyu.backend.domain.map.dto.res;

// pin_location + pin + location 조인 결과를 받는 네이티브 쿼리 프로젝션 인터페이스.
// 메서드 명은 쿼리의 컬럼 alias(camelCase)와 정확히 매핑되어야 합니다.
public interface MapPinView {
    Long getPinId();
    String getPinType();
    Double getLat();      // ST_Y(pin_point) → 위도
    Double getLng();      // ST_X(pin_point) → 경도
    String getDetailAddress();
    String getRegion();   // location.location 컬럼 (법정동코드 10자리 문자열)
}
