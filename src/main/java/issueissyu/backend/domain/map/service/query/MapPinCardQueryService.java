package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.dto.res.MapPinCardResDTO;

public interface MapPinCardQueryService {

    MapPinCardResDTO findPinCard(Long pinId, String currentUserUid);
}
