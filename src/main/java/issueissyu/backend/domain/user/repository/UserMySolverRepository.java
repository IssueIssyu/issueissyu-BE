package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.issue.entity.ProblemSolver;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMySolverRepository extends JpaRepository<ProblemSolver, Long> {

    @Query(
            value =
                    """
                    SELECT p.pin_id AS "pinId",
                           p.pin_title AS "pinTitle",
                           pl.detail_address AS "pinDetailAddress",
                           ip.issue_pin_state AS "issuePinState",
                           ps.created_at AS "createdAt",
                           ps.problem_solver_id AS "problemSolverId"
                    FROM problem_solver ps
                    JOIN issue_pin ip ON ip.issue_pin_id = ps.issue_pin_id
                    JOIN pin p ON p.pin_id = ip.pin_id
                    LEFT JOIN LATERAL (
                        SELECT pl2.detail_address
                        FROM pin_location pl2
                        WHERE pl2.pin_id = p.pin_id
                        ORDER BY pl2.pin_location_id ASC
                        LIMIT 1
                    ) pl ON TRUE
                    WHERE ps.uid = :uid
                      AND (
                          NOT CAST(:applyCursor AS boolean)
                          OR ps.created_at < CAST(:cursorCreatedAt AS timestamp)
                          OR (
                              ps.created_at = CAST(:cursorCreatedAt AS timestamp)
                              AND ps.problem_solver_id < :cursorProblemSolverId
                          )
                      )
                    ORDER BY ps.created_at DESC, ps.problem_solver_id DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<UserMySolverRow> findMySolvers(
            @Param("uid") String uid,
            @Param("applyCursor") boolean applyCursor,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorProblemSolverId") long cursorProblemSolverId,
            @Param("limit") int limit);
}
