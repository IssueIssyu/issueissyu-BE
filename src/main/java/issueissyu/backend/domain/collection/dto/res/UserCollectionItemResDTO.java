package issueissyu.backend.domain.collection.dto.res;

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
    private boolean isLocked;
    private boolean isBookmarked;
    private boolean isProfile;
    private String unlockCondition;
}
