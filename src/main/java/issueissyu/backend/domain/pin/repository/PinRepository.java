package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinRepository extends JpaRepository<Pin, Long> {
}
