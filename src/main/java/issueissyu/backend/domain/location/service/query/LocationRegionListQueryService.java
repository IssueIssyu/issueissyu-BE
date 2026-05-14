package issueissyu.backend.domain.location.service.query;

import issueissyu.backend.domain.location.dto.res.LocationRegionListResDTO;

public interface LocationRegionListQueryService {

    LocationRegionListResDTO getRegionList(String uid);
}
