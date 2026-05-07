package issueissyu.backend.domain.issue.dto.res;

import lombok.Builder;

@Builder
public record ProblemSolverCheckResDTO(
    String problemSolveState
    ) {}
