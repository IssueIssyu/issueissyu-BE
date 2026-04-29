package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OnboardingResDTO {
    private String uuid;

    @JsonProperty("social_type")
    private String socialType;
}
