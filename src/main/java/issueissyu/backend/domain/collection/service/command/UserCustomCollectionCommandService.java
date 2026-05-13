package issueissyu.backend.domain.collection.service.command;

import issueissyu.backend.domain.collection.dto.res.CollectionBookmarkUpdateResDTO;
import issueissyu.backend.domain.collection.dto.res.ProfileCollectionUpdateResDTO;

public interface UserCustomCollectionCommandService {

    ProfileCollectionUpdateResDTO setProfileCollection(String uid, Long collectionId);

    CollectionBookmarkUpdateResDTO setBookmark(String uid, Long collectionId, boolean isBookmarked);
}
