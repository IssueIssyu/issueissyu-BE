package issueissyu.backend.domain.collection.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCollectionItemResDTO {

    private Long collectionId;
    private String name;
    private String imageUrl;
    @JsonProperty("isLocked")
    private boolean isLocked;

    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String unlockCondition;
}
