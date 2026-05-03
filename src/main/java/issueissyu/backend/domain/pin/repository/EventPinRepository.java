package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.EventPin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPinRepository extends JpaRepository<EventPin, Long> {

    Optional<EventPin> findByPin_PinId(Long pinId);
}
