package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.Term;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.mapping.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    Optional<UserTerm> findByUserAndTerm(User user, Term term);
}
