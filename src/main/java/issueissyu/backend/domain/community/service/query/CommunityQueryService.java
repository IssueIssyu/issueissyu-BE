package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.location.enums.RegionCode;

public interface CommunityQueryService {
    CommunityCursorPageResDTO getCommunityFeed(CommunityTab tab, RegionCode region, String cursor, int size);

    CommunityDetailResDTO getCommunityDetail(Long communityId);
}