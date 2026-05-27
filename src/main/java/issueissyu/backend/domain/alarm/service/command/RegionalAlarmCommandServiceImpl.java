package issueissyu.backend.domain.alarm.service.command;

import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.domain.alarm.exception.AlarmException;
import issueissyu.backend.domain.alarm.exception.code.AlarmErrorCode;
import issueissyu.backend.domain.alarm.service.AlarmBatchService;
import issueissyu.backend.domain.alarm.service.FcmService;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.UserLocation;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionalAlarmCommandServiceImpl implements RegionalAlarmCommandService {

    private static final String EVENT_ALARM_BODY_TEMPLATE = "지역 주민 여러분, %s에 놀러오세요.";
    private static final String STORE_ALARM_BODY_TEMPLATE = "%s에서 %s 절찬리 행사중.";
    private static final String STORE_ALARM_BODY_NO_DISCOUNT_TEMPLATE = "%s에서 절찬리 행사중.";

    private final EventPinRepository eventPinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final AlarmBatchService alarmBatchService;
    private final FcmService fcmService;

    @Override
    public void dispatchScheduledEventAlarms() {
        log.info("[RegionalAlarm] dispatchScheduledEventAlarms start");
        LocalDateTime[] window = alarmTimeWindow();

        List<EventPin> targets = eventPinRepository.findAlarmTargetsByPinTypeAndStartTimeBetween(
                PinType.FESTIVAL, window[0], window[1]);

        for (EventPin eventPin : targets) {
            try {
                processEventAlarmForPin(eventPin);
            } catch (Exception e) {
                log.warn(
                        "[RegionalAlarm] event alarm skipped for eventPinId={}: {}",
                        eventPin.getEventPinId(),
                        e.getMessage());
            }
        }
        log.info("[RegionalAlarm] dispatchScheduledEventAlarms done. targets={}", targets.size());
    }

    @Override
    public void dispatchScheduledStoreAlarms() {
        log.info("[RegionalAlarm] dispatchScheduledStoreAlarms start");
        LocalDateTime[] window = alarmTimeWindow();

        List<EventPin> targets = eventPinRepository.findAlarmTargetsByPinTypeAndStartTimeBetween(
                PinType.STORE, window[0], window[1]);

        for (EventPin eventPin : targets) {
            try {
                processStoreAlarmForPin(eventPin);
            } catch (Exception e) {
                log.warn(
                        "[RegionalAlarm] store alarm skipped for eventPinId={}: {}",
                        eventPin.getEventPinId(),
                        e.getMessage());
            }
        }
        log.info("[RegionalAlarm] dispatchScheduledStoreAlarms done. targets={}", targets.size());
    }

    @Override
    @Transactional
    public EventAlarmPrepared sendEventAlarmToUser(String uid) {
        User recipient = findUser(uid);

        if (!recipient.isEventAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.EVENT_ALARM_403);
        }

        Long locationId = resolveUserLocationId(recipient, AlarmErrorCode.EVENT_ALARM_404);
        LocalDateTime[] window = alarmTimeWindow();

        EventPin eventPin = eventPinRepository
                .findAlarmTargetsByPinTypeAndLocationIdAndStartTimeBetween(
                        PinType.FESTIVAL, locationId, window[0], window[1])
                .stream()
                .findFirst()
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.EVENT_ALARM_404));

        Long pinId = eventPin.getPin().getPinId();
        String body = String.format(EVENT_ALARM_BODY_TEMPLATE, eventPin.getPin().getPinTitle());
        Long communityId = resolveCommunityIdOrThrow(pinId, AlarmErrorCode.EVENT_ALARM_404);

        return alarmBatchService.persistEventAlarm(recipient, body, pinId, communityId);
    }

    @Override
    @Transactional
    public StoreAlarmPrepared sendStoreAlarmToUser(String uid) {
        User recipient = findUser(uid);

        if (!recipient.isStoreAlarmActive()) {
            throw AlarmException.of(AlarmErrorCode.STORE_ALARM_403);
        }

        Long locationId = resolveUserLocationId(recipient, AlarmErrorCode.STORE_ALARM_404);
        LocalDateTime[] window = alarmTimeWindow();

        EventPin eventPin = eventPinRepository
                .findAlarmTargetsByPinTypeAndLocationIdAndStartTimeBetween(
                        PinType.STORE, locationId, window[0], window[1])
                .stream()
                .findFirst()
                .orElseThrow(() -> AlarmException.of(AlarmErrorCode.STORE_ALARM_404));

        Long pinId = eventPin.getPin().getPinId();
        String body = buildStoreAlarmBody(eventPin);
        Long communityId = resolveCommunityIdOrThrow(pinId, AlarmErrorCode.STORE_ALARM_404);

        return alarmBatchService.persistStoreAlarm(recipient, body, pinId, communityId);
    }

    private void processEventAlarmForPin(EventPin eventPin) {
        Long pinId = eventPin.getPin().getPinId();
        String body = String.format(EVENT_ALARM_BODY_TEMPLATE, eventPin.getPin().getPinTitle());

        Long locationId = resolvePinLocationId(pinId);
        Long communityId = resolveCommunityIdOrNull(pinId);

        List<User> recipients = userRepository.findEventAlarmEligibleByLocationId(locationId);
        List<FcmNotificationPayload> payloads =
                alarmBatchService.persistEventAlarms(recipients, body, pinId, communityId);

        dispatchFcmBatch(payloads, "event");
    }

    private void processStoreAlarmForPin(EventPin eventPin) {
        Long pinId = eventPin.getPin().getPinId();
        String body = buildStoreAlarmBody(eventPin);

        Long locationId = resolvePinLocationId(pinId);
        Long communityId = resolveCommunityIdOrNull(pinId);

        List<User> recipients = userRepository.findStoreAlarmEligibleByLocationId(locationId);
        List<FcmNotificationPayload> payloads =
                alarmBatchService.persistStoreAlarms(recipients, body, pinId, communityId);

        dispatchFcmBatch(payloads, "store");
    }

    private String buildStoreAlarmBody(EventPin eventPin) {
        String pinTitle = eventPin.getPin().getPinTitle();
        String discount = eventPin.getDiscount();
        return StringUtils.hasText(discount)
                ? String.format(STORE_ALARM_BODY_TEMPLATE, pinTitle, discount)
                : String.format(STORE_ALARM_BODY_NO_DISCOUNT_TEMPLATE, pinTitle);
    }

    private void dispatchFcmBatch(List<FcmNotificationPayload> payloads, String alarmType) {
        if (payloads.isEmpty()) {
            return;
        }
        fcmService.sendNotificationsBatchAsync(payloads);
        log.info("[RegionalAlarm] {} FCM batch dispatched count={}", alarmType, payloads.size());
    }

    private static LocalDateTime[] alarmTimeWindow() {
        LocalDateTime now = LocalDateTime.now();
        return new LocalDateTime[] {now.minusHours(12), now.plusHours(12)};
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

    private Long resolvePinLocationId(Long pinId) {
        return pinLocationRepository
                .findByPin_PinId(pinId)
                .map(PinLocation::getLocation)
                .map(Location::getLocationId)
                .orElseThrow(() -> new IllegalStateException("pin_location not found for pinId=" + pinId));
    }

    private Long resolveCommunityIdOrThrow(Long pinId, AlarmErrorCode notFoundError) {
        return communityRepository
                .findByPin_PinId(pinId)
                .map(Community::getCommunityId)
                .orElseThrow(() -> AlarmException.of(notFoundError));
    }

    private Long resolveCommunityIdOrNull(Long pinId) {
        return communityRepository
                .findByPin_PinId(pinId)
                .map(Community::getCommunityId)
                .orElse(null);
    }
}
