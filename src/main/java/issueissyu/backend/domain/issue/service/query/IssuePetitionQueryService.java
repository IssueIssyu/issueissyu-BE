package issueissyu.backend.domain.issue.service.query;

import issueissyu.backend.domain.issue.dto.res.PetitionStatusResDTO;

public interface IssuePetitionQueryService {

    PetitionStatusResDTO getPetitionStatus(Long pinId, String uid);
}
