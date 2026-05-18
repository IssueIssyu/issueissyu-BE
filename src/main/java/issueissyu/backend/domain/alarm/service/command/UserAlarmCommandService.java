package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.req.EventAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.LikeAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.StoreAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmMessageResDTO;

public interface UserAlarmCommandService {

    void savePushToken(String uid, String fcmPushToken);

    AlarmMessageResDTO sendLikeAlarm(Long likeAlarmId, LikeAlarmReqDTO request);

    AlarmMessageResDTO sendEventAlarm(Long eventAlarmId, EventAlarmReqDTO request);

    AlarmMessageResDTO sendStoreAlarm(Long storeAlarmId, StoreAlarmReqDTO request);
}
