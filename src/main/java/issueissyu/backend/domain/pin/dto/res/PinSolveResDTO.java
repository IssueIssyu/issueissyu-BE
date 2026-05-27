package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import issueissyu.backend.domain.issue.enums.ProblemSolveState;

public record PinSolveResDTO(
        @JsonProperty("isPetitioned") boolean isPetitioned,
        @JsonProperty("userProblemSolverId") Long userProblemSolverId,
        @JsonProperty("userProblemSolveState") ProblemSolveState userProblemSolveState) {}
