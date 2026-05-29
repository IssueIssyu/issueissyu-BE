package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.StoreImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    @EntityGraph(attributePaths = "eventPin")
    Optional<StoreImage> findByEventPin_Pin_PinId(Long pinId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM StoreImage si WHERE si.eventPin.pin.pinId = :pinId")
    void deleteByEventPin_Pin_PinId(@Param("pinId") Long pinId);
}
