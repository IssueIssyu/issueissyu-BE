package issueissyu.backend.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneSendReqDTO {

    @Schema(example = "010-1234-5678")
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 이어야 합니다.")
    private String phone;
}
