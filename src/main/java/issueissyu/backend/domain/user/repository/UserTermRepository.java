package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.Term;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.mapping.UserTerm;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    Optional<UserTerm> findByUserAndTerm(User user, Term term);

    @EntityGraph(attributePaths = "term")
    List<UserTerm> findAllByUserAndTermIn(User user, Collection<Term> terms);

    @Modifying
    @Query("delete from UserTerm ut where ut.user.uid = :uid")
    void deleteByUserUid(@Param("uid") String uid);
}
