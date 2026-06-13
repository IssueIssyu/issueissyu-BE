package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.domain.alarm.dto.res.AlarmConfirmResDTO;
import issueissyu.backend.domain.alarm.dto.res.EventAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.LikeAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.StoreAlarmSendResDTO;
import issueissyu.backend.domain.alarm.enums.AlarmPushType;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.service.FcmService;
import issueissyu.backend.domain.alarm.entity.UserAlarm;
import issueissyu.backend.domain.alarm.repository.UserAlarmRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAlarmCommandServiceImpl implements UserAlarmCommandService {

    private final UserRepository userRepository;
    private final UserAlarmRepository userAlarmRepository;
    private final LikeAlarmCommandService likeAlarmCommandService;
    private final RegionalAlarmCommandService regionalAlarmCommandService;
    private final FcmService fcmService;

    @Override
    @Transactional
    public void savePushToken(String uid, String fcmPushToken) {
        if (!StringUtils.hasText(fcmPushToken)) {
            throw AlarmException.of(AlarmErrorCode.ALARM_TOKEN_400);
        }
        User user = findUser(uid);
        user.updatePushToken(fcmPushToken);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LikeAlarmSendResDTO sendLikeAlarm(String likerUid, Long pinId) {
        LikeAlarmPrepared prepared = likeAlarmCommandService.createLikeAlarmForApi(likerUid, pinId);

        String messageId = null;
        if (StringUtils.hasText(prepared.pushToken())) {
            try {
                messageId = fcmService.sendNotification(buildLikePayload(prepared));
            } catch (Exception e) {
                log.error("Failed to send like alarm for pinId={}: {}", pinId, e.getMessage(), e);
                throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_400);
            }
        }

        return new LikeAlarmSendResDTO(prepared.likeAlarmId(), messageId, pinId);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public EventAlarmSendResDTO sendEventAlarm(String uid) {
        EventAlarmPrepared prepared = regionalAlarmCommandService.sendEventAlarmToUser(uid);

        String messageId = null;
        if (StringUtils.hasText(prepared.pushToken())) {
            try {
                messageId = fcmService.sendNotification(buildEventPayload(prepared));
            } catch (Exception e) {
                log.error("Failed to send event alarm for uid={}: {}", uid, e.getMessage(), e);
                throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_400);
            }
        }

        return new EventAlarmSendResDTO(
                prepared.eventAlarmId(), messageId, prepared.pinId(), prepared.communityId());
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public StoreAlarmSendResDTO sendStoreAlarm(String uid) {
        StoreAlarmPrepared prepared = regionalAlarmCommandService.sendStoreAlarmToUser(uid);

        String messageId = null;
        if (StringUtils.hasText(prepared.pushToken())) {
            try {
                messageId = fcmService.sendNotification(buildStorePayload(prepared));
            } catch (Exception e) {
                log.error("Failed to send store alarm for uid={}: {}", uid, e.getMessage(), e);
                throw AlarmException.of(AlarmErrorCode.STORE_ALARM_400);
            }
        }

        return new StoreAlarmSendResDTO(
                prepared.storeAlarmId(), messageId, prepared.pinId(), prepared.communityId());
    }

    @Override
    @Transactional
    public AlarmConfirmResDTO confirmAlarm(String uid, Long alarmId) {
        UserAlarm userAlarm = userAlarmRepository
                .findByUserAlarmIdAndUser_Uid(alarmId, uid)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.ALARM_CONFIRM_400));

        userAlarm.markConfirmed();
        return new AlarmConfirmResDTO(userAlarm.getUserAlarmId(), userAlarm.isConfirmed());
    }

    private User findUser(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
    }

    private static FcmNotificationPayload buildLikePayload(LikeAlarmPrepared prepared) {
        return FcmNotificationPayload.builder(prepared.pushToken(), prepared.title(), prepared.body())
                .pushType(AlarmPushType.PIN_LIKED)
                .put("likeAlarmId", String.valueOf(prepared.likeAlarmId()))
                .put("pinId", String.valueOf(prepared.pinId()))
                .build();
    }

    private static FcmNotificationPayload buildEventPayload(EventAlarmPrepared prepared) {
        return FcmNotificationPayload.builder(prepared.pushToken(), prepared.title(), prepared.body())
                .pushType(AlarmPushType.PIN_EVENT)
                .put("eventAlarmId", String.valueOf(prepared.eventAlarmId()))
                .put("communityId", toFcmDataValue(prepared.communityId()))
                .build();
    }

    private static FcmNotificationPayload buildStorePayload(StoreAlarmPrepared prepared) {
        return FcmNotificationPayload.builder(prepared.pushToken(), prepared.title(), prepared.body())
                .pushType(AlarmPushType.PIN_STORE_AD)
                .put("storeAlarmId", String.valueOf(prepared.storeAlarmId()))
                .put("communityId", toFcmDataValue(prepared.communityId()))
                .build();
    }

    private static String toFcmDataValue(Long value) {
        return value != null ? String.valueOf(value) : null;
    }
}
