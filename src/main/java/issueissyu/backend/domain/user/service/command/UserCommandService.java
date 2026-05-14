package issueissyu.backend.domain.user.service.command;

import issueissyu.backend.domain.location.dto.res.UserLocationCertResDto;
import issueissyu.backend.domain.user.dto.req.TermReqDTO;
import issueissyu.backend.domain.user.dto.res.TermResDTO;
import issueissyu.backend.domain.user.dto.res.UserAlarmToggleOutcome;
import issueissyu.backend.domain.user.enums.UserAlarmType;

public interface UserCommandService {

    TermResDTO agreeTerms(String uid, TermReqDTO request);

    void changeNickname(String uid, String nickname);

    UserLocationCertResDto changeUserRegion(String uid, double lat, double lng);

    UserAlarmToggleOutcome toggleUserAlarm(String uid, UserAlarmType alarmType);
}
