package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.ComplaintPetition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComplaintPetitionRepository extends JpaRepository<ComplaintPetition, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ComplaintPetition cp WHERE cp.issuePin.issuePinId = :issuePinId")
    void deleteByIssuePin_IssuePinId(@Param("issuePinId") Long issuePinId);
}