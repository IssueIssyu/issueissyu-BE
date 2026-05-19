package issueissyu.backend.domain.alarm.service.command;

public record EventAlarmPrepared(
        Long eventAlarmId,
        Long pinId,
        Long communityId,
        String pushToken,
        String title,
        String body) {}
