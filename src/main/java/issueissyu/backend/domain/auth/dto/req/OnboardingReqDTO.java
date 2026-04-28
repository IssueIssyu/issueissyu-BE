package issueissyu.backend.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OnboardingReqDTO {

    @Schema(example = "string")
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9가-힣]{1,15}$", message = "닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다.")
    private String nickname;

    @Schema(example = "abc@kau.kr")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(example = "010-1234-5678")
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 이어야 합니다.")
    private String phone;
}
