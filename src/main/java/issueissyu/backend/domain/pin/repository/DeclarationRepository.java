package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Declaration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclarationRepository extends JpaRepository<Declaration, Long> {

    boolean existsByPin_PinIdAndUser_Uid(Long pinId, String uid);
}
