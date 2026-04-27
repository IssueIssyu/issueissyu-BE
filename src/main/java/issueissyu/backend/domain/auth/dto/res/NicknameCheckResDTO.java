package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameCheckResDTO {

    @JsonProperty("is_available_nickname")
    private final boolean isAvailableNickname;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String nickname;
}
