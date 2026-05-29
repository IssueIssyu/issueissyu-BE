package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.ComplaintPetition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintPetitionRepository extends JpaRepository<ComplaintPetition, Long> {

    void deleteByIssuePin_IssuePinId(Long issuePinId);
}
