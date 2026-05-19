package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.LikeAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeAlarmRepository extends JpaRepository<LikeAlarm, Long> {
}
