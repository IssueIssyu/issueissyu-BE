package issueissyu.backend.domain.issue.entity;

import issueissyu.backend.domain.issue.enums.ProblemSolveState;
import issueissyu.backend.domain.user.entity.UserEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "problem_solver")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemSolver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_solver_id")
    private Long problemSolverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_pin_id", nullable = false)
    @ToString.Exclude
    private IssuePin issuePin;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_solve_state", nullable = false)
    private ProblemSolveState problemSolveState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @ToString.Exclude
    private UserEntity user;
}
