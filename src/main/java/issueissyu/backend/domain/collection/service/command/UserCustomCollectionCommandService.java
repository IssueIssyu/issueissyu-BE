package issueissyu.backend.domain.collection.service.command;

import issueissyu.backend.domain.collection.dto.res.CollectionBookmarkUpdateResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionUpdateResDTO;

public interface UserCustomCollectionCommandService {

    // 대표 프로필 컬렉션 지정 (나머지 프로필 플래그 해제)
    ProfileCollectionUpdateResDTO setProfileCollection(String uid, Long collectionId);

    // 북마크 설정 (켜면 다른 건 전부 끔)
    CollectionBookmarkUpdateResDTO setBookmark(String uid, Long collectionId, boolean isBookmarked);
}
