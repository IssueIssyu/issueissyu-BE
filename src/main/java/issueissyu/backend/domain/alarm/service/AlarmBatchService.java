package issueissyu.backend.domain.alarm.service;

import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.domain.alarm.entity.EventAlarm;
import issueissyu.backend.domain.alarm.entity.StoreAlarm;
import issueissyu.backend.domain.alarm.entity.UserAlarm;
import issueissyu.backend.domain.alarm.repository.EventAlarmRepository;
import issueissyu.backend.domain.alarm.repository.StoreAlarmRepository;
import issueissyu.backend.domain.alarm.repository.UserAlarmRepository;
import issueissyu.backend.domain.alarm.service.command.EventAlarmPrepared;
import issueissyu.backend.domain.alarm.service.command.StoreAlarmPrepared;
import issueissyu.backend.domain.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlarmBatchService {

    private static final String EVENT_ALARM_TITLE = "🎉 우리동네 축제 알리미!";
    private static final String STORE_ALARM_TITLE = "🏪 반짝 홍보!";

    private final UserAlarmRepository userAlarmRepository;
    private final EventAlarmRepository eventAlarmRepository;
    private final StoreAlarmRepository storeAlarmRepository;

    @Transactional
    public List<FcmNotificationPayload> persistEventAlarms(
            List<User> recipients, String body, String communityId) {
        if (recipients.isEmpty()) {
            return List.of();
        }

        List<UserAlarm> userAlarms = new ArrayList<>(recipients.size());
        for (User recipient : recipients) {
            userAlarms.add(UserAlarm.builder().user(recipient).build());
        }
        userAlarms = userAlarmRepository.saveAll(userAlarms);

        List<EventAlarm> eventAlarms = new ArrayList<>(recipients.size());
        for (int i = 0; i < recipients.size(); i++) {
            eventAlarms.add(EventAlarm.builder()
                    .userAlarm(userAlarms.get(i))
                    .eventAlarmTitle(EVENT_ALARM_TITLE)
                    .eventAlarmBody(body)
                    .build());
        }
        eventAlarms = eventAlarmRepository.saveAll(eventAlarms);

        List<FcmNotificationPayload> payloads = new ArrayList<>(recipients.size());
        for (int i = 0; i < recipients.size(); i++) {
            User user = recipients.get(i);
            EventAlarm eventAlarm = eventAlarms.get(i);
            payloads.add(new FcmNotificationPayload(
                    user.getPushToken(),
                    EVENT_ALARM_TITLE,
                    body,
                    Map.of(
                            "eventAlarmId",
                            String.valueOf(eventAlarm.getEventAlarmId()),
                            "communityId",
                            communityId)));
        }
        return payloads;
    }

    @Transactional
    public List<FcmNotificationPayload> persistStoreAlarms(
            List<User> recipients, String body, String communityId) {
        if (recipients.isEmpty()) {
            return List.of();
        }

        List<UserAlarm> userAlarms = new ArrayList<>(recipients.size());
        for (User recipient : recipients) {
            userAlarms.add(UserAlarm.builder().user(recipient).build());
        }
        userAlarms = userAlarmRepository.saveAll(userAlarms);

        List<StoreAlarm> storeAlarms = new ArrayList<>(recipients.size());
        for (int i = 0; i < recipients.size(); i++) {
            storeAlarms.add(StoreAlarm.builder()
                    .userAlarm(userAlarms.get(i))
                    .storeAlarmTitle(STORE_ALARM_TITLE)
                    .storeAlarmBody(body)
                    .build());
        }
        storeAlarms = storeAlarmRepository.saveAll(storeAlarms);

        List<FcmNotificationPayload> payloads = new ArrayList<>(recipients.size());
        for (int i = 0; i < recipients.size(); i++) {
            User user = recipients.get(i);
            StoreAlarm storeAlarm = storeAlarms.get(i);
            payloads.add(new FcmNotificationPayload(
                    user.getPushToken(),
                    STORE_ALARM_TITLE,
                    body,
                    Map.of(
                            "storeAlarmId",
                            String.valueOf(storeAlarm.getStoreAlarmId()),
                            "communityId",
                            communityId)));
        }
        return payloads;
    }

    @Transactional
    public EventAlarmPrepared persistEventAlarm(User recipient, String body, Long pinId, Long communityId) {
        UserAlarm userAlarm = userAlarmRepository.save(UserAlarm.builder().user(recipient).build());

        EventAlarm eventAlarm = eventAlarmRepository.save(
                EventAlarm.builder()
                        .userAlarm(userAlarm)
                        .eventAlarmTitle(EVENT_ALARM_TITLE)
                        .eventAlarmBody(body)
                        .build());

        return new EventAlarmPrepared(
                eventAlarm.getEventAlarmId(),
                pinId,
                communityId,
                recipient.getPushToken(),
                EVENT_ALARM_TITLE,
                body);
    }

    @Transactional
    public StoreAlarmPrepared persistStoreAlarm(User recipient, String body, Long pinId, Long communityId) {
        UserAlarm userAlarm = userAlarmRepository.save(UserAlarm.builder().user(recipient).build());

        StoreAlarm storeAlarm = storeAlarmRepository.save(
                StoreAlarm.builder()
                        .userAlarm(userAlarm)
                        .storeAlarmTitle(STORE_ALARM_TITLE)
                        .storeAlarmBody(body)
                        .build());

        return new StoreAlarmPrepared(
                storeAlarm.getStoreAlarmId(),
                pinId,
                communityId,
                recipient.getPushToken(),
                STORE_ALARM_TITLE,
                body);
    }
}
