package issueissyu.backend.domain.issue.service.command;

import issueissyu.backend.domain.issue.dto.res.PetitionSubmitResDTO;

public interface IssuePetitionCommandService {

    PetitionSubmitResDTO submitPetition(Long pinId, String uid);
}
