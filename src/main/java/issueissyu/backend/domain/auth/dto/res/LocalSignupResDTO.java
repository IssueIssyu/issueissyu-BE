package issueissyu.backend.domain.auth.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocalSignupResDTO {

    private String email;
}
