package issueissyu.backend.domain.issue.dto.res;

import lombok.Builder;

@Builder
public record ProblemSolverItemResDTO(
        long problemSolverId,
        String problemSolveState,
        String problemSolverImageUrl,
        String nickname,
        String createdAt,
        String profileUrl,
        String checkAction) {}
