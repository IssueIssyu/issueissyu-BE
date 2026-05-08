package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.ProblemSolver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemSolverRepository extends JpaRepository<ProblemSolver, Long> {

    boolean existsByIssuePin_Pin_PinIdAndUser_Uid(Long pinId, String uid);

    @Query(
            """
            select ps from ProblemSolver ps
            join fetch ps.user su
            join fetch ps.issuePin ip
            join fetch ip.pin p
            where p.pinId = :pinId
            order by case when ps.problemSolveState = 'RESOLVED' then 0 else 1 end, ps.createdAt asc""")
    List<ProblemSolver> findAllForPinWithAssociations(@Param("pinId") Long pinId);

    @Query(
            """
            select ps from ProblemSolver ps
            join fetch ps.user u
            left join fetch ps.problemSolverImage img
            where ps.problemSolverId = :id""")
    Optional<ProblemSolver> fetchWithUserAndImage(@Param("id") Long id);

    @Query(
            """
            select ps from ProblemSolver ps
            join fetch ps.issuePin ip
            join fetch ip.pin p
            join fetch p.user pu
            join fetch ps.user su
            where ps.problemSolverId = :id""")
    Optional<ProblemSolver> fetchWithPinOwnerAndSolver(@Param("id") Long id);
}
