package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.IssuePetition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuePetitionRepository extends JpaRepository<IssuePetition, Long> {

    boolean existsByIssuePin_Pin_PinIdAndUser_Uid(Long pinId, String uid);

    void deleteByIssuePin_IssuePinId(Long issuePinId);
}
