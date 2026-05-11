package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Declaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeclarationRepository extends JpaRepository<Declaration, Long> {

    boolean existsByPin_PinIdAndUser_Uid(Long pinId, String uid);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Declaration d WHERE d.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);
}
