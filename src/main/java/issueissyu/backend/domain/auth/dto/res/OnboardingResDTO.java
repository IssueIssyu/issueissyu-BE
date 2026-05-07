package issueissyu.backend.domain.auth.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OnboardingResDTO {
    private String uuid;
    private String socialType;
    private Long userCustomCollectionId;
    private Long customCollectionId;
    private String customCollectionName;
    private String customCollectionUrl;
}
