package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.Term;
import issueissyu.backend.domain.user.enums.TermName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findByTermName(TermName termName);
}
