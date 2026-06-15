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

    // 기존 설정 시각
    //@Scheduled(cron = "0 0 13 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 15 22 * * *", zone = "Asia/Seoul")
    public void sendEventAlarms() {
        regionalAlarmCommandService.dispatchScheduledEventAlarms();
    }

    // 기존 설정 시각
    //@Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 15 22 * * *", zone = "Asia/Seoul")
    public void sendStoreAlarms() {
        regionalAlarmCommandService.dispatchScheduledStoreAlarms();
    }

    // 기존 설정 시각
    //@Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 15 22 * * *", zone = "Asia/Seoul")
    public void sendHotAlarms() {
        hotAlarmCommandService.dispatchScheduledHotAlarms();
    }
}
