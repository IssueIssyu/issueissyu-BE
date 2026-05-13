package issueissyu.backend.domain.collection.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyCollectionsResDTO {

    private String nickname;
    private ProfileCollectionSummaryResDTO profileCollection;
    private List<UserCollectionItemResDTO> collections;
}
