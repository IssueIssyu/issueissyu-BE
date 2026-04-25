package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Emogji;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmogjiRepository extends JpaRepository<Emogji, Long> {
    Optional<Emogji> findByProductId(String productId);
}
