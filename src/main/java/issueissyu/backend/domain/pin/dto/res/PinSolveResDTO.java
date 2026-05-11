package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinSolveResDTO(
        @JsonProperty("isPetition") boolean isPetition,
        @JsonProperty("isProblemSolver") boolean isProblemSolver) {}
