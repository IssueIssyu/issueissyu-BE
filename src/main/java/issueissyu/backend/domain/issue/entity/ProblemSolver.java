package issueissyu.backend.domain.issue.entity;

import issueissyu.backend.domain.issue.enums.ProblemSolveState;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "problem_solver")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemSolver extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_solver_id")
    private Long problemSolverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_pin_id", nullable = false)
    @ToString.Exclude
    private IssuePin issuePin;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "problem_solve_state", nullable = false)
    private ProblemSolveState problemSolveState = ProblemSolveState.EN_ROUTE;

    @OneToOne(mappedBy = "problemSolver", fetch = FetchType.LAZY)
    @ToString.Exclude
    private ProblemSolverImage problemSolverImage;

    public void markVerified() {
        this.problemSolveState = ProblemSolveState.VERIFIED;
    }

    public void markResolved() {
        this.problemSolveState = ProblemSolveState.RESOLVED;
    }
}
