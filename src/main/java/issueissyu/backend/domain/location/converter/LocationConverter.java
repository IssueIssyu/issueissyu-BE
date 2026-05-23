package issueissyu.backend.domain.location.converter;

import issueissyu.backend.domain.location.dto.res.LocationRegionResDTO;
import issueissyu.backend.domain.location.entity.Location;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationConverter {

    public static LocationRegionResDTO toRegionResDTO(Location location) {
        return new LocationRegionResDTO(location.getRegion());
    }
}
