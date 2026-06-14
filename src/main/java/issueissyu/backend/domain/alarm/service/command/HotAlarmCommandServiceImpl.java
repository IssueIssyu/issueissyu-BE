package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.service.AlarmBatchService;
import issueissyu.backend.domain.alarm.service.FcmService;
import issueissyu.backend.domain.community.dto.HotCommunityTarget;
import issueissyu.backend.domain.community.service.query.CommunityHotQueryService;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.UserLocation;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotAlarmCommandServiceImpl implements HotAlarmCommandService {

    static final String HOT_ALARM_TITLE = "📢 우리동네 핫 이슈!";
    static final String HOT_ALARM_BODY_TEMPLATE = "[HOT] %s에 %d명이 관심을 보이고 있어요.";

    private final UserRepository userRepository;
    private final CommunityHotQueryService communityHotQueryService;
    private final AlarmBatchService alarmBatchService;
    private final FcmService fcmService;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void dispatchScheduledHotAlarms() {
        log.info("[HotAlarm] dispatchScheduledHotAlarms start");

        List<Long> locationIds = userRepository.findDistinctHotAlarmEligibleLocationIds();

        for (Long locationId : locationIds) {
            try {
                processHotAlarmForLocation(locationId);
            } catch (Exception e) {
                log.warn("[HotAlarm] hot alarm skipped for locationId={}: {}", locationId, e.getMessage());
            }
        }

        log.info("[HotAlarm] dispatchScheduledHotAlarms done. locations={}", locationIds.size());
    }

    @Override
    @Transactional
    public HotAlarmPrepared sendHotAlarmToUser(String uid) {
        User recipient = findUser(uid);

        if (!recipient.isHotAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.HOT_ALARM_403);
        }

        Long locationId = resolveUserLocationId(recipient, AlarmErrorCode.HOT_ALARM_404);
        HotCommunityTarget target = communityHotQueryService
                .findTopHotInRegion(locationId)
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.HOT_ALARM_404));

        String body = buildHotAlarmBody(target);

        return alarmBatchService.persistHotAlarm(
                recipient, HOT_ALARM_TITLE, body, target.pinId(), target.communityId());
    }

    private void processHotAlarmForLocation(Long locationId) {
        HotCommunityTarget target = communityHotQueryService
                .findTopHotInRegion(locationId)
                .orElse(null);
        if (target == null) {
            return;
        }

        List<User> recipients = userRepository.findHotAlarmEligibleByLocationId(locationId);
        if (recipients.isEmpty()) {
            return;
        }

        String body = buildHotAlarmBody(target);
        List<FcmNotificationPayload> payloads = alarmBatchService.persistHotAlarms(
                recipients, HOT_ALARM_TITLE, body, target.pinId(), target.communityId());

        if (payloads.isEmpty()) {
            return;
        }

        fcmService.sendNotificationsBatchAsync(payloads);
        log.info("[HotAlarm] FCM batch dispatched locationId={} count={}", locationId, payloads.size());
    }

    private static String buildHotAlarmBody(HotCommunityTarget target) {
        return String.format(HOT_ALARM_BODY_TEMPLATE, target.pinTitle(), target.viewCount());
    }

    private User findUser(String uid) {
        return userRepository
                .findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
    }

    private Long resolveUserLocationId(User user, AlarmErrorCode noLocationError) {
        UserLocation userLocation = user.getUserLocation();
        if (userLocation == null || userLocation.getLocation() == null) {
            throw AlarmException.of(noLocationError);
        }
        return userLocation.getLocation().getLocationId();
    }
}
