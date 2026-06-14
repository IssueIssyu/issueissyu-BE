package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.HotAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotAlarmRepository extends JpaRepository<HotAlarm, Long> {}
