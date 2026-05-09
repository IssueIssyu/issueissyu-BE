package issueissyu.backend.domain.pin.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record DeclarationReqDTO(
        @Min(1) @Max(5) int reasonIndex
) {
}
