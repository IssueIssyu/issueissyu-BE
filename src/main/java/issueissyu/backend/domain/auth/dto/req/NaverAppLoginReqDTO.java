package issueissyu.backend.domain.auth.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 앱(안드로이드)에서 네이버 SDK로 발급받은 토큰을 백엔드로 전달하는 요청 DTO
@Getter
@NoArgsConstructor
public class NaverAppLoginReqDTO {

    @JsonProperty("accessToken")
    @NotBlank
    private String accessToken;

    @JsonProperty("refreshToken")
    @NotBlank
    private String refreshToken;
}
