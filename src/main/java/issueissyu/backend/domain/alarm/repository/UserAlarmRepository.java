package issueissyu.backend.domain.alarm.repository;

import issueissyu.backend.domain.alarm.entity.UserAlarm;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAlarmRepository extends JpaRepository<UserAlarm, Long> {

    Optional<UserAlarm> findByUserAlarmIdAndUser_Uid(Long userAlarmId, String uid);
}
