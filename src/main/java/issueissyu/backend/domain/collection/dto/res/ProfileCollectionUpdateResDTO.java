package issueissyu.backend.domain.collection.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileCollectionUpdateResDTO {

    private Long profileCollectionId;
    private String profileImageUrl;
}
