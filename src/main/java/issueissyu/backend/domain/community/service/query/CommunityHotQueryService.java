package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.HotCommunityTarget;
import java.util.Optional;

public interface CommunityHotQueryService {

    Optional<HotCommunityTarget> findTopHotInRegion(Long locationId);
}
