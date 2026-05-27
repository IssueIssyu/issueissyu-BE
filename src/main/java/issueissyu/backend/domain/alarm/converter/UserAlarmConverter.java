package issueissyu.backend.domain.alarm.converter;

import issueissyu.backend.domain.alarm.dto.res.AlarmListItemResDTO;
import issueissyu.backend.domain.alarm.repository.UserAlarmListRow;
import issueissyu.backend.domain.alarm.support.AlarmTimeAgoFormatter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAlarmConverter {

    private final AlarmTimeAgoFormatter alarmTimeAgoFormatter;

    public AlarmListItemResDTO toListItem(UserAlarmListRow row, LocalDateTime now) {
        return new AlarmListItemResDTO(
                row.getAlarmId(),
                Boolean.TRUE.equals(row.getIsConfirmed()),
                row.getAlarmType(),
                row.getAlarmTitle(),
                row.getAlarmBody(),
                row.getPinId(),
                row.getCommunityId(),
                row.getCreatedAt(),
                alarmTimeAgoFormatter.format(row.getCreatedAt(), now));
    }
}
