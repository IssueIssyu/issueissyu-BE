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
public class NaverReverseGeocodeReqDTO {

    private PGpoint point;
    private String sourcecrs;
    private String targetcrs;
    private String orders;
    private String output;
    private String callback;

    public static NaverReverseGeocodeReqDTO of(PGpoint point) {
        return NaverReverseGeocodeReqDTO.builder()
                .point(point)
                .build();
    }

    public String toCoords() {
        if (point == null) {
            return null;
        }
        return point.x + "," + point.y;
    }
}
