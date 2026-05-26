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

    // 전체 컬렉션 카탈로그(ID 순). isLocked·isBookmarked 포함. 마이페이지 노출 필터는 클라이언트에서 적용.
    private List<UserCollectionItemResDTO> collections;

    // checkUnlock=true 요청에서 이번에 새로 해금된 컬렉션 (연출용)
    private List<NewlyUnlockedCollectionResDTO> newlyUnlocked;
}
