package issueissyu.backend.domain.alarm.service;

import issueissyu.backend.domain.alarm.service.command.HotAlarmCommandService;
import issueissyu.backend.domain.alarm.service.command.RegionalAlarmCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSchedulerService {

    private final RegionalAlarmCommandService regionalAlarmCommandService;
    private final HotAlarmCommandService hotAlarmCommandService;

    @Scheduled(cron = "0 0 13 * * *")
    public void sendEventAlarms() {
        regionalAlarmCommandService.dispatchScheduledEventAlarms();
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendStoreAlarms() {
        regionalAlarmCommandService.dispatchScheduledStoreAlarms();
    }

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void sendHotAlarms() {
        hotAlarmCommandService.dispatchScheduledHotAlarms();
    }
}
