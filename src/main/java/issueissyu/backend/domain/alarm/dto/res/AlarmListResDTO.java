package issueissyu.backend.domain.alarm.dto.res;

import java.util.List;

public record AlarmListResDTO(
    List<AlarmListItemResDTO> alarms, 
    AlarmListPageInfoResDTO pageInfo
    ) {}
