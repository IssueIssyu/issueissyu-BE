package issueissyu.backend.domain.collection.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CollectionBookmarkUpdateResDTO {

    private Long customCollectionId;
    private boolean isBookmarked;
}
