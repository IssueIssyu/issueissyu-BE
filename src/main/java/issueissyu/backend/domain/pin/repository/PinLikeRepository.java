package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.mapping.PinLike;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PinLikeRepository extends JpaRepository<PinLike, Long> {

    boolean existsByPin_PinIdAndUser_Uid(Long pinId, String uid);

    @EntityGraph(attributePaths = "user")
    List<PinLike> findByPin_PinId(Long pinId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PinLike pl WHERE pl.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);
}
