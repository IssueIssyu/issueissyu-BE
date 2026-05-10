package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.EventPin;
import java.util.Collection;
import java.util.List;
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

    @Query("select ep from EventPin ep left join fetch ep.storeImage where ep.pin.pinId in :pinIds")
    List<EventPin> findWithStoreImageByPinIdIn(@Param("pinIds") Collection<Long> pinIds);
}
