package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenPairDTO {

    @JsonProperty("accessToken")
    private final String accessToken;

    @JsonProperty("refreshToken")
    private final String refreshToken;
}
