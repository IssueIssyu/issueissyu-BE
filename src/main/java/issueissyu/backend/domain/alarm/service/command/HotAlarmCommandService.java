package issueissyu.backend.domain.alarm.service.command;

public interface HotAlarmCommandService {

    void dispatchScheduledHotAlarms();

    HotAlarmPrepared sendHotAlarmToUser(String uid);
}
