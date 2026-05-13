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

    // 대표 프로필 컬렉션 요약 (없으면 null)
    private ProfileCollectionSummaryResDTO myCollection;

    // 전체 컬렉션 목록(ID 순), 해금·북마크 반영
    private List<UserCollectionItemResDTO> collections;
}
