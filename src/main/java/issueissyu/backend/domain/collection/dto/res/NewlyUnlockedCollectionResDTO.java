package issueissyu.backend.domain.collection.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NewlyUnlockedCollectionResDTO {

    private Long collectionId;
    private String name;
    private String imageUrl;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String unlockCondition;
}
