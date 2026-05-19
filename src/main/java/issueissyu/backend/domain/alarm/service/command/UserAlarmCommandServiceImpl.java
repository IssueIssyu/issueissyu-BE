package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.req.EventAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.StoreAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmMessageResDTO;
import issueissyu.backend.domain.alarm.dto.res.LikeAlarmSendResDTO;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.service.FcmService;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.util.Map;
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
    private final UserAlarmSendQueryService userAlarmSendQueryService;
    private final LikeAlarmCommandService likeAlarmCommandService;
    private final FcmService fcmService;

    @Override
    @Transactional
    public void savePushToken(String uid, String fcmPushToken) {
        if (!StringUtils.hasText(fcmPushToken)) {
            throw AlarmException.of(AlarmErrorCode.PUSH_TOKEN_400);
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
                messageId = fcmService.sendNotification(
                        prepared.pushToken(),
                        prepared.title(),
                        prepared.body(),
                        Map.of(
                                "likeAlarmId",
                                String.valueOf(prepared.likeAlarmId()),
                                "pinId",
                                String.valueOf(prepared.pinId())));
            } catch (Exception e) {
                log.error("Failed to send like alarm for pinId={}: {}", pinId, e.getMessage(), e);
                throw AlarmException.of(AlarmErrorCode.LIKE_ALARM_400);
            }
        }

        return new LikeAlarmSendResDTO(prepared.likeAlarmId(), messageId, pinId);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AlarmMessageResDTO sendEventAlarm(Long eventAlarmId, EventAlarmReqDTO request) {
        String pushToken = userAlarmSendQueryService.resolveEventAlarmPushToken(eventAlarmId);

        try {
            String messageId = fcmService.sendNotification(
                    pushToken,
                    request.eventAlarmTitle(),
                    request.eventAlarmBody(),
                    Map.of("eventAlarmId", String.valueOf(eventAlarmId)));
            return new AlarmMessageResDTO(messageId);
        } catch (Exception e) {
            log.error("Failed to send event alarm for id={}: {}", eventAlarmId, e.getMessage(), e);
            throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_400);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AlarmMessageResDTO sendStoreAlarm(Long storeAlarmId, StoreAlarmReqDTO request) {
        String pushToken = userAlarmSendQueryService.resolveStoreAlarmPushToken(storeAlarmId);

        try {
            String messageId = fcmService.sendNotification(
                    pushToken,
                    request.storeAlarmTitle(),
                    request.storeAlarmBody(),
                    Map.of("storeAlarmId", String.valueOf(storeAlarmId)));
            return new AlarmMessageResDTO(messageId);
        } catch (Exception e) {
            log.error("Failed to send store alarm for id={}: {}", storeAlarmId, e.getMessage(), e);
            throw AlarmException.of(AlarmErrorCode.STORE_ALARM_400);
        }
    }

    private User findUser(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
    }
}
