package issueissyu.backend.domain.auth.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameCheckReqDTO {

    @JsonProperty("nickname")
    private String nickname;
}
