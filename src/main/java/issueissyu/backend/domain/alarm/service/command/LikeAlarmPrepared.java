package issueissyu.backend.domain.alarm.service.command;

public record LikeAlarmPrepared(
        Long likeAlarmId, Long pinId, String recipientUid, String pushToken, String title, String body) {}
