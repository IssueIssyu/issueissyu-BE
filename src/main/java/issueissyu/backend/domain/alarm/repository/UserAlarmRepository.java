package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.UserAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAlarmRepository extends JpaRepository<UserAlarm, Long> {
}
