package issueissyu.backend.domain.location.service.query;

import issueissyu.backend.domain.location.dto.res.LocationRegionResDTO;

public interface LocationQueryService {

    LocationRegionResDTO getRegionByLocationId(Long locationId);
}
