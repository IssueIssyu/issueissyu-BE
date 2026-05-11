package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Pin;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PinRepository extends JpaRepository<Pin, Long> {

    @EntityGraph(attributePaths = {"user", "pinImages"})
    @Query("select p from Pin p where p.pinId = :id")
    Optional<Pin> fetchDetailWithAuthor(@Param("id") Long pinId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pin p where p.pinId = :pinId")
    Optional<Pin> findWithPessimisticWriteByPinId(@Param("pinId") Long pinId);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("update Pin p set p.likeCount = p.likeCount + 1 where p.pinId = :pinId")
    int incrementLikeCountByPinId(@Param("pinId") Long pinId);

    @Query("select p.likeCount from Pin p where p.pinId = :pinId")
    Optional<Integer> findLikeCountByPinId(@Param("pinId") Long pinId);
}
