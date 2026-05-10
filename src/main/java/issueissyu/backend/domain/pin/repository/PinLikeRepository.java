package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.mapping.PinLike;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PinLikeRepository extends JpaRepository<PinLike, Long> {

    boolean existsByPin_PinIdAndUser_Uid(Long pinId, String uid);

    @EntityGraph(attributePaths = "user")
    List<PinLike> findByPin_PinId(Long pinId);

    void deleteByPin_PinId(Long pinId);
}
