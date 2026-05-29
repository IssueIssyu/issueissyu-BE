package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.IssuePetition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssuePetitionRepository extends JpaRepository<IssuePetition, Long> {

    boolean existsByIssuePin_Pin_PinIdAndUser_Uid(Long pinId, String uid);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM IssuePetition ip WHERE ip.issuePin.issuePinId = :issuePinId")
    void deleteByIssuePin_IssuePinId(@Param("issuePinId") Long issuePinId);
}
