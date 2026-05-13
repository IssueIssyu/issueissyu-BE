package issueissyu.backend.domain.collection.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CollectionBookmarkUpdateResDTO {

    private Long customCollectionId;

    @JsonProperty("isBookmarked")
    private boolean isBookmarked;
}
