package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.CommunicationPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommunicationPinRepository extends JpaRepository<CommunicationPin, Long> {

    Optional<CommunicationPin> findByPin_PinId(Long pinId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CommunicationPin c WHERE c.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE CommunicationPin c
            SET c.updatedAt = :ts
            WHERE c.pin.pinId = :pinId
            """)
    int bumpUpdatedAt(@Param("pinId") Long pinId, @Param("ts") LocalDateTime ts);

}
