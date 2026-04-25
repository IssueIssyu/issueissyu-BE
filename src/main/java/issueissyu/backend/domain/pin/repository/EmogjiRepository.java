package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Emogji;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmogjiRepository extends JpaRepository<Emogji, Long> {
}
