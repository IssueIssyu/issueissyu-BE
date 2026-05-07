package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.EventPin;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventPinRepository extends JpaRepository<EventPin, Long> {

    Optional<EventPin> findByPin_PinId(Long pinId);

    @EntityGraph(attributePaths = "storeImage")
    @Query("select ep from EventPin ep where ep.pin.pinId = :pinId")
    Optional<EventPin> findWithStoreImageByPinPinId(@Param("pinId") Long pinId);
}
