package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityHomeResDTO;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;

public interface CommunityQueryService {
    CommunityHomeResDTO getCommunityHome(
            String uid,
            Long locationId,
            String recentCursor,
            int storeSize,
            int recentSize
    );

    CommunityCursorPageResDTO getCommunityFeed(CommunityTab tab, String uid, Long locationId, String cursor, int size);

    CommunityDetailResult getCommunityDetail(Long communityId, CommunityType kind, String uid);

    record CommunityDetailResult(CommunitySuccessCode successCode, CommunityDetailResDTO data) {}
}