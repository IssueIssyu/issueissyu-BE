package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.req.EventAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.StoreAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmMessageResDTO;
import issueissyu.backend.domain.alarm.dto.res.LikeAlarmSendResDTO;

public interface UserAlarmCommandService {

    void savePushToken(String uid, String fcmPushToken);

    LikeAlarmSendResDTO sendLikeAlarm(String likerUid, Long pinId);

    AlarmMessageResDTO sendEventAlarm(Long eventAlarmId, EventAlarmReqDTO request);

    AlarmMessageResDTO sendStoreAlarm(Long storeAlarmId, StoreAlarmReqDTO request);
}
