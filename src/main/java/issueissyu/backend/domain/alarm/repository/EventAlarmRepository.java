package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.EventAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAlarmRepository extends JpaRepository<EventAlarm, Long> {
}
