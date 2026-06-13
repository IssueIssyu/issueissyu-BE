package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.res.AlarmConfirmResDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmListResDTO;
import issueissyu.backend.domain.alarm.dto.res.EventAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.LikeAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.StoreAlarmSendResDTO;

public interface UserAlarmCommandService {

    void savePushToken(String uid, String fcmPushToken);

    LikeAlarmSendResDTO sendLikeAlarm(String likerUid, Long pinId);

    EventAlarmSendResDTO sendEventAlarm(String uid);

    StoreAlarmSendResDTO sendStoreAlarm(String uid);

    AlarmConfirmResDTO confirmAlarm(String uid, Long alarmId);

    void confirmUnconfirmedAlarmsInList(String uid, AlarmListResDTO alarmList);
}
