package issueissyu.backend.domain.alarm.service;

import issueissyu.backend.domain.alarm.entity.EventAlarm;
import issueissyu.backend.domain.alarm.entity.StoreAlarm;
import issueissyu.backend.domain.alarm.entity.UserAlarm;
import issueissyu.backend.domain.alarm.repository.EventAlarmRepository;
import issueissyu.backend.domain.alarm.repository.StoreAlarmRepository;
import issueissyu.backend.domain.alarm.repository.UserAlarmRepository;
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
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSchedulerService {

    private static final String EVENT_ALARM_TITLE = "🎉 우리동네 축제 알리미!";
    private static final String EVENT_ALARM_BODY_TEMPLATE = "지역 주민 여러분, %s에 놀러오세요.";

    private static final String STORE_ALARM_TITLE = "🏪 반짝 홍보!";
    private static final String STORE_ALARM_BODY_TEMPLATE = "%s에서 %s 절찬리 행사중.";
    private static final String STORE_ALARM_BODY_NO_DISCOUNT_TEMPLATE = "%s에서 절찬리 행사중.";

    private final EventPinRepository eventPinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final UserAlarmRepository userAlarmRepository;
    private final EventAlarmRepository eventAlarmRepository;
    private final StoreAlarmRepository storeAlarmRepository;
    private final FcmService fcmService;

    // 매일 13시 — FESTIVAL 핀 기준 이벤트 푸시 알람
    @Scheduled(cron = "0 0 13 * * *")
    @Transactional
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
                log.warn("[AlarmScheduler] event alarm skipped for eventPinId={}: {}",
                        eventPin.getEventPinId(), e.getMessage());
            }
        }
        log.info("[AlarmScheduler] sendEventAlarms done. targets={}", targets.size());
    }

    // 매일 10시 — STORE 핀 기준 가게 홍보 푸시 알람
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
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
                log.warn("[AlarmScheduler] store alarm skipped for eventPinId={}: {}",
                        eventPin.getEventPinId(), e.getMessage());
            }
        }
        log.info("[AlarmScheduler] sendStoreAlarms done. targets={}", targets.size());
    }

    // ───────────────────────── private ─────────────────────────

    private void processEventAlarm(EventPin eventPin) {
        Long pinId = eventPin.getPin().getPinId();
        String pinTitle = eventPin.getPin().getPinTitle();
        String body = String.format(EVENT_ALARM_BODY_TEMPLATE, pinTitle);

        Long locationId = resolveLocationId(pinId);
        String communityId = resolveCommunityId(pinId);

        List<User> recipients = userRepository.findEventAlarmEligibleByLocationId(locationId);
        for (User user : recipients) {
            try {
                createAndSendEventAlarm(user, body, communityId);
            } catch (Exception e) {
                log.warn("[AlarmScheduler] event FCM failed uid={}: {}", user.getUid(), e.getMessage());
            }
        }
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
        for (User user : recipients) {
            try {
                createAndSendStoreAlarm(user, body, communityId);
            } catch (Exception e) {
                log.warn("[AlarmScheduler] store FCM failed uid={}: {}", user.getUid(), e.getMessage());
            }
        }
    }

    private void createAndSendEventAlarm(User user, String body, String communityId) {
        UserAlarm userAlarm = userAlarmRepository.save(UserAlarm.builder().user(user).build());
        EventAlarm eventAlarm = eventAlarmRepository.save(
                EventAlarm.builder()
                        .userAlarm(userAlarm)
                        .eventAlarmTitle(EVENT_ALARM_TITLE)
                        .eventAlarmBody(body)
                        .build());

        if (!StringUtils.hasText(user.getPushToken())) return;

        fcmService.sendNotification(
                user.getPushToken(),
                EVENT_ALARM_TITLE,
                body,
                Map.of("eventAlarmId", String.valueOf(eventAlarm.getEventAlarmId()),
                        "communityId", communityId));
    }

    private void createAndSendStoreAlarm(User user, String body, String communityId) {
        UserAlarm userAlarm = userAlarmRepository.save(UserAlarm.builder().user(user).build());
        StoreAlarm storeAlarm = storeAlarmRepository.save(
                StoreAlarm.builder()
                        .userAlarm(userAlarm)
                        .storeAlarmTitle(STORE_ALARM_TITLE)
                        .storeAlarmBody(body)
                        .build());

        if (!StringUtils.hasText(user.getPushToken())) return;

        fcmService.sendNotification(
                user.getPushToken(),
                STORE_ALARM_TITLE,
                body,
                Map.of("storeAlarmId", String.valueOf(storeAlarm.getStoreAlarmId()),
                        "communityId", communityId));
    }

    private Long resolveLocationId(Long pinId) {
        return pinLocationRepository.findByPin_PinId(pinId)
                .map(PinLocation::getLocation)
                .map(loc -> loc.getLocationId())
                .orElseThrow(() -> new IllegalStateException("pin_location not found for pinId=" + pinId));
    }

    private String resolveCommunityId(Long pinId) {
        return communityRepository.findByPin_PinId(pinId)
                .map(Community::getCommunityId)
                .map(String::valueOf)
                .orElse("");
    }
}
