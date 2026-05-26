package issueissyu.backend.domain.collection.service.command;

import issueissyu.backend.domain.collection.dto.res.NewlyUnlockedCollectionResDTO;
import java.util.List;

public interface CollectionUnlockService {

    List<NewlyUnlockedCollectionResDTO> evaluateAndUnlockMissions(String uid);
}
