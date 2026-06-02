package issueissyu.backend.domain.alarm.listener;

import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.domain.alarm.enums.AlarmPushType;
import issueissyu.backend.domain.alarm.event.LikeAlarmCreatedEvent;
import issueissyu.backend.domain.alarm.service.FcmService;
import issueissyu.backend.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeAlarmNotificationListener {

    private final FcmService fcmService;

    @Async(AsyncConfig.ALARM_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeAlarmCreated(LikeAlarmCreatedEvent event) {
        if (!StringUtils.hasText(event.pushToken())) {
            return;
        }

        fcmService
                .sendNotificationAsync(buildLikePayload(event))
                .whenComplete((messageId, ex) -> {
                    if (ex != null) {
                        log.warn(
                                "Like FCM send failed for uid={}: {}",
                                event.recipientUid(),
                                ex.getMessage());
                    } else {
                        log.debug("Like FCM sent uid={} messageId={}", event.recipientUid(), messageId);
                    }
                });
    }

    private static FcmNotificationPayload buildLikePayload(LikeAlarmCreatedEvent event) {
        return FcmNotificationPayload.builder(event.pushToken(), event.title(), event.body())
                .pushType(AlarmPushType.PIN_LIKED)
                .put("likeAlarmId", String.valueOf(event.likeAlarmId()))
                .put("pinId", String.valueOf(event.pinId()))
                .build();
    }
}
