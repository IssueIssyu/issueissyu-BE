package issueissyu.backend.domain.alarm.repository;

import java.time.LocalDateTime;

public interface UserAlarmListRow {

    Long getAlarmId();

    Boolean getIsConfirmed();

    String getAlarmType();

    String getAlarmTitle();

    String getAlarmBody();

    Long getPinId();

    Long getCommunityId();

    LocalDateTime getCreatedAt();
}
