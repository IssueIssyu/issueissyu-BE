package issueissyu.backend.domain.alarm.service.command;

public record StoreAlarmPrepared(
        Long storeAlarmId,
        Long pinId,
        Long communityId,
        String pushToken,
        String title,
        String body) {}
