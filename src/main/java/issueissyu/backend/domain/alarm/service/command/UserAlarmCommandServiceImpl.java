package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.req.EventAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.LikeAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.StoreAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmMessageResDTO;
import issueissyu.backend.domain.alarm.entity.EventAlarm;
import issueissyu.backend.domain.alarm.entity.LikeAlarm;
import issueissyu.backend.domain.alarm.entity.StoreAlarm;
import issueissyu.backend.domain.alarm.entity.UserAlarm;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.repository.EventAlarmRepository;
import issueissyu.backend.domain.alarm.repository.LikeAlarmRepository;
import issueissyu.backend.domain.alarm.repository.StoreAlarmRepository;
import issueissyu.backend.domain.alarm.service.FcmService;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAlarmCommandServiceImpl implements UserAlarmCommandService {

    private final UserRepository userRepository;
    private final LikeAlarmRepository likeAlarmRepository;
    private final EventAlarmRepository eventAlarmRepository;
    private final StoreAlarmRepository storeAlarmRepository;
    private final FcmService fcmService;

    @Override
    public void savePushToken(String uid, String fcmPushToken) {
        if (!StringUtils.hasText(fcmPushToken)) {
            throw AlarmException.of(AlarmErrorCode.PUSH_TOKEN_400);
        }
        User user = findUser(uid);
        user.updatePushToken(fcmPushToken);
    }

    @Override
    public AlarmMessageResDTO sendLikeAlarm(Long likeAlarmId, LikeAlarmReqDTO request) {
        LikeAlarm likeAlarm = likeAlarmRepository.findById(likeAlarmId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.LIKE_ALARM_404));

        User recipient = likeAlarm.getUserAlarm().getUser();

        if (!recipient.isLikeAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_403);
        }

        String pushToken = recipient.getPushToken();
        if (!StringUtils.hasText(pushToken)) {
            throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_400);
        }

        try {
            String messageId = fcmService.sendNotification(
                    pushToken,
                    request.likeAlarmTitle(),
                    request.likeAlarmBody(),
                    Map.of("likeAlarmId", String.valueOf(likeAlarmId)));
            return new AlarmMessageResDTO(messageId);
        } catch (Exception e) {
            throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_400);
        }
    }

    @Override
    public AlarmMessageResDTO sendEventAlarm(Long eventAlarmId, EventAlarmReqDTO request) {
        EventAlarm eventAlarm = eventAlarmRepository.findById(eventAlarmId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.EVENT_ALARM_404));

        User recipient = eventAlarm.getUserAlarm().getUser();

        if (!recipient.isEventAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_403);
        }

        String pushToken = recipient.getPushToken();
        if (!StringUtils.hasText(pushToken)) {
            throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_400);
        }

        try {
            String messageId = fcmService.sendNotification(
                    pushToken,
                    request.eventAlarmTitle(),
                    request.eventAlarmBody(),
                    Map.of("eventAlarmId", String.valueOf(eventAlarmId)));
            return new AlarmMessageResDTO(messageId);
        } catch (Exception e) {
            throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_400);
        }
    }

    @Override
    public AlarmMessageResDTO sendStoreAlarm(Long storeAlarmId, StoreAlarmReqDTO request) {
        StoreAlarm storeAlarm = storeAlarmRepository.findById(storeAlarmId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.STORE_ALARM_404));

        User recipient = storeAlarm.getUserAlarm().getUser();

        if (!recipient.isStoreAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.STORE_ALARM_403);
        }

        String pushToken = recipient.getPushToken();
        if (!StringUtils.hasText(pushToken)) {
            throw AlarmException.of(AlarmErrorCode.STORE_ALARM_400);
        }

        try {
            String messageId = fcmService.sendNotification(
                    pushToken,
                    request.storeAlarmTitle(),
                    request.storeAlarmBody(),
                    Map.of("storeAlarmId", String.valueOf(storeAlarmId)));
            return new AlarmMessageResDTO(messageId);
        } catch (Exception e) {
            throw AlarmException.of(AlarmErrorCode.STORE_ALARM_400);
        }
    }

    private User findUser(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
    }
}
