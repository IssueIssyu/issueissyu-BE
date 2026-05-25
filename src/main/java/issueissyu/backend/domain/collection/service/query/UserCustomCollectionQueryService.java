package issueissyu.backend.domain.collection.service.query;

import issueissyu.backend.domain.collection.dto.res.MyCollectionsResDTO;

public interface UserCustomCollectionQueryService {
    MyCollectionsResDTO getMyCollections(String uid, boolean checkUnlock);
}
