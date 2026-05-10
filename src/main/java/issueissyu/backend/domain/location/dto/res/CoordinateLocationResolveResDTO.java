package issueissyu.backend.domain.location.dto.res;

/** EPSG:4326 좌표로 역지오코딩한 도로명 주소 및 DB {@code Location} 식별자 */
public record CoordinateLocationResolveResDTO(Long locationId, String address) {
}
