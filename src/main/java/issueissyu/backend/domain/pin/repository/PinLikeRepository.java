package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.mapping.PinLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinLikeRepository extends JpaRepository<PinLike, Long> {

    boolean existsByPin_PinIdAndUser_Uid(Long pinId, String uid);
}
