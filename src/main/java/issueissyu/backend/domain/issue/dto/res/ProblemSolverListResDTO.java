package issueissyu.backend.domain.issue.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Builder
public record ProblemSolverListResDTO(
    Boolean isGoNow, 
    List<ProblemSolverItemResDTO> problemSolvers
    ) {}
