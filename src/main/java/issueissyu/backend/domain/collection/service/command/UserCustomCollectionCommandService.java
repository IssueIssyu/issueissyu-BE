package issueissyu.backend.domain.collection.service.command;

import issueissyu.backend.domain.collection.dto.res.CollectionBookmarkUpdateResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionUpdateResDTO;

public interface UserCustomCollectionCommandService {

    // 대표 프로필 컬렉션 지정 (나머지 프로필 플래그 해제)
    ProfileCollectionUpdateResDTO setProfileCollection(String uid, Long collectionId);

    // 해금된 컬렉션 북마크 on/off (복수 가능)
    CollectionBookmarkUpdateResDTO setBookmark(String uid, Long collectionId, boolean isBookmarked);
}
