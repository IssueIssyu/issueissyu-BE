package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityHomeResDTO;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;

public interface CommunityQueryService {
    CommunityHomeResDTO getCommunityHome(
            String region,
            String recentCursor,
            int storeSize,
            int recentSize
    );

    CommunityCursorPageResDTO getCommunityFeed(CommunityTab tab, String region, String cursor, int size);

    CommunityDetailResult getCommunityDetail(Long communityId, String uid);

    record CommunityDetailResult(CommunitySuccessCode successCode, CommunityDetailResDTO data) {}
}