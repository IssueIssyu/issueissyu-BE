package issueissyu.backend.domain.pin.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinSolveResDTO(
        @JsonProperty("isPetitioned") boolean isPetitioned,
        @JsonProperty("isProblemSolver") boolean isProblemSolver) {}
