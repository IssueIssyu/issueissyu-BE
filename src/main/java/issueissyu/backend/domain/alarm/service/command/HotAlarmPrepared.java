package issueissyu.backend.domain.alarm.service.command;

public record HotAlarmPrepared(
        Long hotAlarmId, Long pinId, Long communityId, String pushToken, String title, String body) {}
