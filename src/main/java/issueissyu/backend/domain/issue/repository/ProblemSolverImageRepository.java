package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.ProblemSolverImage;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemSolverImageRepository extends JpaRepository<ProblemSolverImage, Long> {

    Optional<ProblemSolverImage> findByProblemSolver_ProblemSolverId(Long problemSolverId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProblemSolverImage psi WHERE psi.problemSolver.problemSolverId IN :ids")
    void deleteAllByProblemSolver_ProblemSolverIdIn(@Param("ids") Collection<Long> ids);
}
