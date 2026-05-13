package issueissyu.backend.domain.collection.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileCollectionSummaryResDTO {

    private Long collectionId;
    private String name;
    private String imageUrl;
}
