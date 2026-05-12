package issueissyu.backend.domain.user.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TermReqDTO {

    @NotNull
    @JsonProperty("serviceTerm")
    private Boolean serviceTerm;

    @NotNull
    @JsonProperty("privacyTerm")
    private Boolean privacyTerm;

    @NotNull
    @JsonProperty("locationTerm")
    private Boolean locationTerm;

    @NotNull
    @JsonProperty("marketingTerm")
    private Boolean marketingTerm;
}
