package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.IssuePin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuePinRepository extends JpaRepository<IssuePin, Long> {

    Optional<IssuePin> findByPin_PinId(Long pinId);
}
