package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.CommunicationPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface CommunicationPinRepository extends JpaRepository<CommunicationPin, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE CommunicationPin c
            SET c.updatedAt = :ts
            WHERE c.pin.pinId = :pinId
            """)
    int bumpUpdatedAt(@Param("pinId") Long pinId, @Param("ts") LocalDateTime ts);
}
