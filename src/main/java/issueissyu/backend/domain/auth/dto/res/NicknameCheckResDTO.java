package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameCheckResDTO {

    @Getter(AccessLevel.NONE)
    private final boolean isAvailableNickname;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String nickname;

    @JsonProperty("isAvailableNickname")
    public boolean isAvailableNickname() {
        return isAvailableNickname;
    }
}
