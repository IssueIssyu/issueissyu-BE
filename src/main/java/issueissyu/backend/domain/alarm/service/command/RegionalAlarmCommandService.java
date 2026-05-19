package issueissyu.backend.domain.alarm.service.command;

public interface RegionalAlarmCommandService {

    void dispatchScheduledEventAlarms();

    void dispatchScheduledStoreAlarms();

    EventAlarmPrepared sendEventAlarmToUser(String uid);

    StoreAlarmPrepared sendStoreAlarmToUser(String uid);
}
