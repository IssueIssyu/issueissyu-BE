package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.IssuePin;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssuePinRepository extends JpaRepository<IssuePin, Long> {

    Optional<IssuePin> findByPin_PinId(Long pinId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ip from IssuePin ip join fetch ip.pin p where p.pinId = :pinId")
    Optional<IssuePin> findWithPessimisticWriteByPinId(@Param("pinId") Long pinId);
}
