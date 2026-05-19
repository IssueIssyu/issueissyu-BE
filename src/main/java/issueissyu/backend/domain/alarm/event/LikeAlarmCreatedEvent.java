package issueissyu.backend.domain.alarm.event;

public record LikeAlarmCreatedEvent(
        String recipientUid,
        String pushToken,
        Long likeAlarmId,
        Long pinId,
        String title,
        String body) {}
