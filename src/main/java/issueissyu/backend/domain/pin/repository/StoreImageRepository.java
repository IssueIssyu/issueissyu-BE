package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.StoreImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    @EntityGraph(attributePaths = "eventPin")
    Optional<StoreImage> findByEventPin_Pin_PinId(Long pinId);
}
