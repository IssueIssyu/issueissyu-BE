package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.dto.res.MapNoticeListResDTO;

public interface MapNoticeQueryService {

    MapNoticeListResDTO getActiveNotices();
}
