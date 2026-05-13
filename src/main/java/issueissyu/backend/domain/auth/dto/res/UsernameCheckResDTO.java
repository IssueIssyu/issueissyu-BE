package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsernameCheckResDTO {

    @Getter(AccessLevel.NONE)
    private final boolean isAvailableUsername;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String userName;

    @JsonProperty("isAvailableUsername")
    public boolean isAvailableUsername() {
        return isAvailableUsername;
    }
}
