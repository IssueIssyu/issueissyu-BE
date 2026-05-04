package issueissyu.backend.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OnboardingResDTO {
    private String uuid;

    @JsonProperty("social_type")
    private String socialType;

    @JsonProperty("user_custom_collection_id")
    private Long userCustomCollectionId;

    @JsonProperty("custom_collection_id")
    private Long customCollectionId;

    @JsonProperty("custom_collection_name")
    private String customCollectionName;
}
