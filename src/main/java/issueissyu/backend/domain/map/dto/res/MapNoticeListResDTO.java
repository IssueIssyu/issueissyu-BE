package issueissyu.backend.domain.map.dto.res;

import java.util.List;
import lombok.Builder;

@Builder
public record MapNoticeListResDTO(List<MapNoticeItemResDTO> notices) {}
