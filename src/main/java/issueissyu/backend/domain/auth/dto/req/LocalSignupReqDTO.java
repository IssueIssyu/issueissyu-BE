package issueissyu.backend.domain.auth.dto.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocalSignupReqDTO {

    @JsonProperty("userName")
    @Schema(example = "myid01")
    @NotBlank(message = "아이디를 입력해주세요.")
    private String userName;

    @JsonProperty("password")
    @Schema(example = "string")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
