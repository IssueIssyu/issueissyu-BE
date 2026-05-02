package issueissyu.backend.domain.auth.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import issueissyu.backend.domain.user.enums.SocialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginLinkReqDTO {

    @NotNull(message = "소셜 로그인 타입을 입력해주세요.")
    @JsonProperty("social_type")
    @Schema(example = "LOCAL")
    private SocialType socialType;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Schema(example = "010-XXXX-XXXX")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 이어야 합니다.")
    private String phone;
}
