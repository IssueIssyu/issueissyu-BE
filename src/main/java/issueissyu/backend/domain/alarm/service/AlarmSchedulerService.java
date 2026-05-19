package issueissyu.backend.domain.alarm.service;

import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSchedulerService {

    private static final String EVENT_ALARM_BODY_TEMPLATE = "지역 주민 여러분, %s에 놀러오세요.";
    private static final String STORE_ALARM_BODY_TEMPLATE = "%s에서 %s 절찬리 행사중.";
    private static final String STORE_ALARM_BODY_NO_DISCOUNT_TEMPLATE = "%s에서 절찬리 행사중.";

    private final EventPinRepository eventPinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final AlarmBatchService alarmBatchService;
    private final FcmService fcmService;

    @Scheduled(cron = "0 0 13 * * *")
    public void sendEventAlarms() {
        log.info("[AlarmScheduler] sendEventAlarms start");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(12);
        LocalDateTime to = now.plusHours(12);

        List<EventPin> targets = eventPinRepository.findAlarmTargetsByPinTypeAndStartTimeBetween(
                PinType.FESTIVAL, from, to);

        for (EventPin eventPin : targets) {
            try {
                processEventAlarm(eventPin);
            } catch (Exception e) {
                log.warn(
                        "[AlarmScheduler] event alarm skipped for eventPinId={}: {}",
                        eventPin.getEventPinId(),
                        e.getMessage());
            }
        }
        log.info("[AlarmScheduler] sendEventAlarms done. targets={}", targets.size());
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendStoreAlarms() {
        log.info("[AlarmScheduler] sendStoreAlarms start");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(12);
        LocalDateTime to = now.plusHours(12);

        List<EventPin> targets = eventPinRepository.findAlarmTargetsByPinTypeAndStartTimeBetween(
                PinType.STORE, from, to);

        for (EventPin eventPin : targets) {
            try {
                processStoreAlarm(eventPin);
            } catch (Exception e) {
                log.warn(
                        "[AlarmScheduler] store alarm skipped for eventPinId={}: {}",
                        eventPin.getEventPinId(),
                        e.getMessage());
            }
        }
        log.info("[AlarmScheduler] sendStoreAlarms done. targets={}", targets.size());
    }

    private void processEventAlarm(EventPin eventPin) {
        Long pinId = eventPin.getPin().getPinId();
        String body = String.format(EVENT_ALARM_BODY_TEMPLATE, eventPin.getPin().getPinTitle());

        Long locationId = resolveLocationId(pinId);
        String communityId = resolveCommunityId(pinId);

        List<User> recipients = userRepository.findEventAlarmEligibleByLocationId(locationId);
        List<FcmNotificationPayload> payloads =
                alarmBatchService.persistEventAlarms(recipients, body, communityId);

        dispatchFcmBatch(payloads, "event");
    }

    private void processStoreAlarm(EventPin eventPin) {
        Long pinId = eventPin.getPin().getPinId();
        String pinTitle = eventPin.getPin().getPinTitle();
        String discount = eventPin.getDiscount();

        String body = StringUtils.hasText(discount)
                ? String.format(STORE_ALARM_BODY_TEMPLATE, pinTitle, discount)
                : String.format(STORE_ALARM_BODY_NO_DISCOUNT_TEMPLATE, pinTitle);

        Long locationId = resolveLocationId(pinId);
        String communityId = resolveCommunityId(pinId);

        List<User> recipients = userRepository.findStoreAlarmEligibleByLocationId(locationId);
        List<FcmNotificationPayload> payloads =
                alarmBatchService.persistStoreAlarms(recipients, body, communityId);

        dispatchFcmBatch(payloads, "store");
    }

    // DB 커밋 후 FCM은 비동기 일괄 전송 — 스케줄러 스레드 즉시 반환
    private void dispatchFcmBatch(List<FcmNotificationPayload> payloads, String alarmType) {
        if (payloads.isEmpty()) {
            return;
        }
        fcmService.sendNotificationsBatchAsync(payloads);
        log.info("[AlarmScheduler] {} FCM batch dispatched count={}", alarmType, payloads.size());
    }

    private Long resolveLocationId(Long pinId) {
        return pinLocationRepository
                .findByPin_PinId(pinId)
                .map(PinLocation::getLocation)
                .map(loc -> loc.getLocationId())
                .orElseThrow(() -> new IllegalStateException("pin_location not found for pinId=" + pinId));
    }

    private String resolveCommunityId(Long pinId) {
        return communityRepository
                .findByPin_PinId(pinId)
                .map(Community::getCommunityId)
                .map(String::valueOf)
                .orElse("");
    }
}
