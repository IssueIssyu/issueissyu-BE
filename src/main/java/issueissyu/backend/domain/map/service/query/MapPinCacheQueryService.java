package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.dto.res.MapPinCacheStatusResDTO;

public interface MapPinCacheQueryService {

    MapPinCacheStatusResDTO getCacheStatus(String uid);
}
