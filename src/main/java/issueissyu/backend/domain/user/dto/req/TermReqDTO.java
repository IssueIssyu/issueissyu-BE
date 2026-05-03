package issueissyu.backend.domain.user.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TermReqDTO {

    @NotNull
    @JsonProperty("service_term")
    private Boolean serviceTerm;

    @NotNull
    @JsonProperty("privacy_term")
    private Boolean privacyTerm;

    @NotNull
    @JsonProperty("location_term")
    private Boolean locationTerm;

    @NotNull
    @JsonProperty("marketing_term")
    private Boolean marketingTerm;
}
