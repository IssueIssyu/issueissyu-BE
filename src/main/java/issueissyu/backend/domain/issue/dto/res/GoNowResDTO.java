package issueissyu.backend.domain.issue.dto.res;

import lombok.Builder;

@Builder
public record GoNowResDTO(
    long pinId, 
    long problemSolverId, 
    String problemSolveState
    ) {}
