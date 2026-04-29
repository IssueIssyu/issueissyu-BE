package issueissyu.backend.domain.location.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.postgresql.geometric.PGpoint;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverGeocodeReqDTO {

    private String query;
    private String coordinate;
    private String filter;
    private String language;
    private Integer page;
    private Integer count;

    public static NaverGeocodeReqDTO of(String query) {
        return NaverGeocodeReqDTO.builder()
                .query(query)
                .build();
    }

    public static NaverGeocodeReqDTO of(String query, PGpoint point) {
        return NaverGeocodeReqDTO.builder()
                .query(query)
                .coordinate(point == null ? null : point.x + "," + point.y)
                .build();
    }
}
