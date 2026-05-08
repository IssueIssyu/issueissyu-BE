package issueissyu.backend.domain.issue.repository;

import issueissyu.backend.domain.issue.entity.ProblemSolverImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemSolverImageRepository extends JpaRepository<ProblemSolverImage, Long> {

    Optional<ProblemSolverImage> findByProblemSolver_ProblemSolverId(Long problemSolverId);
}
