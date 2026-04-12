package issueissyu.backend.domain.issue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "problem_solver_image")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemSolverImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_solver_image_id")
    private Long problemSolverImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_solver_id", nullable = false)
    @ToString.Exclude
    private ProblemSolver problemSolver;

    @Column(name = "problem_solver_image_url", nullable = false, length = 255)
    private String problemSolverImageUrl;
}
