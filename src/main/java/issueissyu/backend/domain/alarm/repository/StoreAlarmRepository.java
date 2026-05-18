package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.StoreAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreAlarmRepository extends JpaRepository<StoreAlarm, Long> {
}
