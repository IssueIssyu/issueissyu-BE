package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.entity.EventAlarm;
import issueissyu.backend.domain.alarm.entity.StoreAlarm;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.repository.EventAlarmRepository;
import issueissyu.backend.domain.alarm.repository.StoreAlarmRepository;
import issueissyu.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAlarmSendQueryService {

    private final EventAlarmRepository eventAlarmRepository;
    private final StoreAlarmRepository storeAlarmRepository;

    public String resolveEventAlarmPushToken(Long eventAlarmId) {
        EventAlarm eventAlarm = eventAlarmRepository.findById(eventAlarmId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.EVENT_ALARM_404));

        User recipient = eventAlarm.getUserAlarm().getUser();

        if (!recipient.isEventAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_403);
        }

        return requirePushToken(recipient.getPushToken(), AlarmErrorCode.EVENT_ALARM_400);
    }

    public String resolveStoreAlarmPushToken(Long storeAlarmId) {
        StoreAlarm storeAlarm = storeAlarmRepository.findById(storeAlarmId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.STORE_ALARM_404));

        User recipient = storeAlarm.getUserAlarm().getUser();

        if (!recipient.isStoreAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.STORE_ALARM_403);
        }

        return requirePushToken(recipient.getPushToken(), AlarmErrorCode.STORE_ALARM_400);
    }

    private String requirePushToken(String pushToken, AlarmErrorCode missingTokenError) {
        if (!StringUtils.hasText(pushToken)) {
            throw AlarmException.of(missingTokenError);
        }
        return pushToken;
    }
}
