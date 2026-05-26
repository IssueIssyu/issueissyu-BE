package issueissyu.backend.domain.alarm.service.query;

import issueissyu.backend.domain.alarm.dto.res.AlarmListResDTO;

public interface UserAlarmQueryService {

    AlarmListResDTO getAlarmList(String uid, Integer size, String cursor);
}
