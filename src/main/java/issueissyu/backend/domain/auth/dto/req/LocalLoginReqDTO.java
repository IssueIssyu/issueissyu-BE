package issueissyu.backend.domain.auth.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocalLoginReqDTO {

    @JsonProperty("userName")
    @NotBlank
    private String userName;

    @JsonProperty("password")
    @NotBlank
    private String password;
}
