package issueissyu.backend.domain.issue.dto.res;

import lombok.Builder;

@Builder
public record ProblemSolverPhotoResDTO(
    long photoId, 
    String photoUrl, 
    String problemSolveState
    ) {}
