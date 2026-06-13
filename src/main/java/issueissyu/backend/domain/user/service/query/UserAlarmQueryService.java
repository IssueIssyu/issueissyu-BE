package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.user.dto.res.UserAlarmStateResDTO;

public interface UserAlarmQueryService {

    UserAlarmStateResDTO getAlarmState(String uid);
}
