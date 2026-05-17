package issueissyu.backend.domain.user.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameChangeReqDTO {

    @NotBlank
    @JsonProperty("nickname")
    private String nickname;
}
