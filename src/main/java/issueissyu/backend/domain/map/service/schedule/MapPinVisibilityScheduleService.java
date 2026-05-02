package issueissyu.backend.domain.map.service.schedule;

import issueissyu.backend.domain.pin.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MapPinVisibilityScheduleService {

    private final PinRepository pinRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void refreshPinVisibilityForMap() {
        pinRepository.hidePinsRegisteredOverOneYearAgo();
        pinRepository.updateCommunicationPinVisibilityBySchedule();
        pinRepository.updateEventPinVisibilityBySchedule();
    }
}
