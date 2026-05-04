package issueissyu.backend.domain.location.repository;

import issueissyu.backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
